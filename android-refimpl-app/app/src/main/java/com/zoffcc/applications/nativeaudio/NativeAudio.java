/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.nativeaudio;

import android.util.Log;

import com.zoffcc.applications.trifa.AudioRecording;

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.AudioRecording.microphone_muted;
import static com.zoffcc.applications.trifa.MainActivity.audio_buffer_2_ts;

public class NativeAudio
{
    private static final String TAG = "trifa.NativeAudio";

    public static final int n_audio_in_buffer_max_count = 10;
    public static ByteBuffer[] n_audio_buffer = new ByteBuffer[n_audio_in_buffer_max_count];
    public static int n_cur_buf = 0;
    public static int n_buf_size_in_bytes = 0;
    public static int[] n_bytes_in_buffer = new int[n_audio_in_buffer_max_count];
    public static int sampling_rate = 44100;
    public static int channel_count = 2;

    public static final int n_rec_audio_in_buffer_max_count = 10;
    public static ByteBuffer[] n_rec_audio_buffer = new ByteBuffer[n_rec_audio_in_buffer_max_count];
    public static int n_rec_cur_buf = 0;
    public static int n_rec_buf_size_in_bytes = 0;
    public static int[] n_rec_bytes_in_buffer = new int[n_rec_audio_in_buffer_max_count];

    public static boolean native_audio_engine_down = false;

    /**
     * problem switching to audio only
     *
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:--> EAR
     * com.zoffcc.applications.trifa I/trifa.ToxService: I:setting filteraudio_active=0
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:setMode(AudioManager.MODE_IN_COMMUNICATION)
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: turnOffScreen
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:turnOffScreen()
     * com.zoffcc.applications.trifa I/System.out: AVCS:VOICE:1
     * com.zoffcc.applications.trifa I/trifa.nativeaudio: player_state:res_011=0 SL_RESULT_SUCCESS=0 PAUSED
     * com.zoffcc.applications.trifa W/AudioSystem: AudioPolicyService server died!
     * com.zoffcc.applications.trifa W/AudioSystem: AudioFlinger server died!
     * com.zoffcc.applications.trifa W/AudioRecord: dead IAudioRecord, creating a new one from obtainBuffer()
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_policy...
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_flinger...
     * com.zoffcc.applications.trifa I/ServiceManager: Waiting for service media.audio_policy...
     * com.zoffcc.applications.trifa D/AudioSystem: make AudioPortCallbacksEnabled to TRUE
     * com.zoffcc.applications.trifa E/AudioRecord: AudioFlinger could not create record track, status: -22
     * com.zoffcc.applications.trifa W/AudioRecord: restoreRecord_l() failed status -22
     * com.zoffcc.applications.trifa E/AudioRecord: Error -22 obtaining an audio buffer, giving up.
     * com.zoffcc.applications.trifa D/SensorManager: Proximity, val = 8.0  [far]
     * com.zoffcc.applications.trifa I/trifa.CallingActivity: onSensorChanged:--> speaker
     * com.zoffcc.applications.trifa I/trifa.ToxService: I:setting filteraudio_active=1
     *
     */

    public static void restartNativeAudioPlayEngine(int sampleRate, int channels)
    {
        System.out.println("restartNativeAudioPlayEngine:sampleRate=" + sampleRate + " channels=" + channels);

        native_audio_engine_down = true;

        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // if (isPlaying() == 1)
        //{
        NativeAudio.StopPCM16();
        //}

        // if (isRecording() == 1)
        //{
        NativeAudio.StopREC();
        //}
        NativeAudio.shutdownEngine();

        System.out.println("restartNativeAudioPlayEngine:startup ...");

        try
        {
            Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        NativeAudio.createEngine(n_audio_in_buffer_max_count);
        NativeAudio.createBufferQueueAudioPlayer(sampleRate, channels, n_audio_in_buffer_max_count);
        NativeAudio.createAudioRecorder((int) AudioRecording.SMAPLINGRATE_TOX, n_rec_audio_in_buffer_max_count);

        NativeAudio.n_cur_buf = 0;

        for (int i = 0; i < n_audio_in_buffer_max_count; i++)
        {
            n_bytes_in_buffer[i] = 0;
        }

        native_audio_engine_down = false;
        NativeAudio.StartREC();
    }

    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

    public static void rec_buffer_ready(int rec_buffer_num)
    {
        // Log.i(TAG, "rec_buffer_ready:num=" + rec_buffer_num);
        // TODO: workaround. sometimes mute button does not mute mic? find a real fix
        if (!microphone_muted)
        {
            // Log.i(TAG, "rec_buffer_ready:002");
            new AudioRecording.send_audio_frame_to_toxcore_from_native(rec_buffer_num).execute();
        }
    }

    public static int PlayPCM16_(int buf_num)
    {
        Log.i(TAG, "PlayPCM16_:play_delay=" + (System.currentTimeMillis() - audio_buffer_2_ts[buf_num]));
        return PlayPCM16(buf_num);
    }

    /**
     * Native methods, implemented in jni folder
     */
    public static native void createEngine(int num_bufs);

    // ---------------------

    public static native void createBufferQueueAudioPlayer(int sampleRate, int channels, int num_bufs);

    public static native void set_JNI_audio_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native int PlayPCM16(int buf_num);

    public static native boolean StopPCM16();

    public static native int isPlaying();

    // public static native boolean enableReverb(boolean enabled);

    // ---------------------

    public static native void createAudioRecorder(int sampleRate, int num_bufs);

    public static native void set_JNI_audio_rec_buffer(ByteBuffer buffer, long buffer_size_in_bytes, int num);

    public static native int isRecording();

    public static native int StartREC();

    public static native boolean StopREC();

    // ---------------------

    public static native void shutdownEngine();
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------
    // ------- DEBUG -------

}
