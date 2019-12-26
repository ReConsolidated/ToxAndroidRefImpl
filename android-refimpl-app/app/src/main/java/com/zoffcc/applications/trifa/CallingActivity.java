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

package com.zoffcc.applications.trifa;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.etiennelawlor.discreteslider.library.ui.DiscreteSlider;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_misc_button_enabled;
import static com.zoffcc.applications.trifa.MainActivity.PREF__allow_screen_off_in_audio_call;
import static com.zoffcc.applications.trifa.MainActivity.PREF__audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_H264_hw_encoding;
import static com.zoffcc.applications.trifa.MainActivity.PREF__use_software_aec;
import static com.zoffcc.applications.trifa.MainActivity.PREF__video_play_delay_ms;
import static com.zoffcc.applications.trifa.MainActivity.PREF__window_security;
import static com.zoffcc.applications.trifa.MainActivity.audio_manager_s;
import static com.zoffcc.applications.trifa.MainActivity.format_timeduration_from_seconds;
import static com.zoffcc.applications.trifa.MainActivity.get_vfs_image_filename_friend_avatar;
import static com.zoffcc.applications.trifa.MainActivity.put_vfs_image_on_imageview;
import static com.zoffcc.applications.trifa.MainActivity.set_audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.set_filteraudio_active;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.toxav_answer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.MainActivity.update_bitrates;
import static com.zoffcc.applications.trifa.MainActivity.update_fps;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_HIGH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_LOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_BITRATE_MED;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_HIGH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_LOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_ENCODER_MAX_QUANTIZER_MED;

public class CallingActivity extends AppCompatActivity implements CameraWrapper.CamOpenOverCallback, SensorEventListener
{
    // private static final boolean AUTO_HIDE = true;
    // private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    // private static final int UI_ANIMATION_DELAY = 300;
    private static final int FRONT_CAMERA_USED = 1;
    private static final int BACK_CAMERA_USED = 2;
    static int active_camera_type = FRONT_CAMERA_USED;
    // private final Handler mHideHandler = new Handler();
    static CustomVideoImageView mContentView;
    static ViewGroup calling_activity_top_viewgroup_vg;
    static ImageView caller_avatar_view;
    static ImageButton accept_button = null;
    ImageButton decline_button = null;
    static ImageButton camera_toggle_button = null;
    static ImageButton mute_button = null;
    ImageButton misc_button = null;
    TextView misc_button_pad = null;
    static ImageView audio_device_icon = null;
    static TextView top_text_line = null;
    static CallingActivity ca = null;
    static String top_text_line_str1 = "";
    static String top_text_line_str2 = "";
    static String top_text_line_str3 = "";
    static String top_text_line_str4 = "";
    Handler callactivity_handler = null;
    static Handler callactivity_handler_s = null;
    static boolean trifa_is_MicrophoneMute = false;
    private static final String TAG = "trifa.CallingActivity";
    // Camera camera = null;
    static CameraSurfacePreview cameraSurfacePreview = null;
    static float mPreviewRate = -1f;
    // static int front_camera_id = -1;
    // static int back_camera_id = -1;
    // static int active_camera_id = 0;
    static AudioRecording audio_thread = null;
    static AudioReceiver audio_receiver_thread = null;
    private SensorManager sensor_manager = null;
    private Sensor proximity_sensor = null;
    PowerManager pm = null;
    PowerManager.WakeLock wl1 = null;
    PowerManager.WakeLock wl2 = null;
    TextView right_top_text_1 = null;
    TextView right_top_text_1b = null;
    TextView right_top_text_2 = null;
    TextView right_top_text_3 = null;
    TextView right_top_text_4 = null;
    TextView right_left_text_1 = null;
    View box_right_volumeslider_01 = null;
    static SeekBar volume_slider_seekbar_01 = null;
    View box_right_video_add_delay_slider_01 = null;
    static SeekBar video_add_delay_slider_seekbar_01 = null;
    static TextView video_add_delay_slider_infotext_01 = null;
    static int activity_state = 0;
    com.etiennelawlor.discreteslider.library.ui.DiscreteSlider quality_slider = null;
    static int quality_slider_position = 0;
    TextView text_vq_low = null;
    TextView text_vq_med = null;
    TextView text_vq_high = null;
    static View video_box_self_preview_01 = null;
    static View video_box_left_top_01 = null;
    static View video_box_right_top_01 = null;
    final static String MIME_TYPE = "video/avc";   // H.264 Advanced Video Coding
    final static int FRAME_RATE = 25;              // ~25fps
    final static int IFRAME_INTERVAL = 6;          // 6 seconds between I-frames
    private static MediaCodec.BufferInfo mBufferInfo;
    private static MediaCodec mEncoder;
    private static MediaPlayer mMediaPlayer = null;
    private static MediaFormat video_encoder_format = null;
    private static int video_encoder_width = 64;
    private static int video_encoder_height = 64;
    private static int v_bitrate_bits_per_second = 20 * 1000; // video bitrate <n> bps, in bits per second
    public static byte[] global_sps_pps_nal_unit_bytes = null;
    public static int send_sps_pps_every_x_frames_current = 0;
    public static int send_sps_pps_every_x_frames = 20;
    private static float slider_alpha = 0.4f;

    private static MediaCodec.BufferInfo mBufferInfo_h264_decoder;
    private static MediaCodec mDecoder_h264;
    private static MediaFormat video_decoder_h264_format = null;
    private DetectHeadset dh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // startActivity called from non-Activity context
        Log.i(TAG, "onCreate:01");

        if (Build.VERSION.SDK_INT >= 27)
        {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null)
            {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }
        else
        {

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                 WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate:02");

        dh = new DetectHeadset(this);

        setContentView(R.layout.activity_calling);

        SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PREF__video_play_delay_ms = settings_cs1.getInt("video_play_delay_ms", 0);
        Log.i(TAG, "pref:get:PREF__video_play_delay_ms=" + PREF__video_play_delay_ms);
        PREF__audio_play_volume_percent = settings_cs1.getInt("audio_play_volume_percent", 100);
        Log.i(TAG, "pref:get:PREF__audio_play_volume_percent=" + PREF__audio_play_volume_percent);

        top_text_line = (TextView) findViewById(R.id.top_text_line);
        accept_button = (ImageButton) findViewById(R.id.accept_button);
        decline_button = (ImageButton) findViewById(R.id.decline_button);
        camera_toggle_button = (ImageButton) findViewById(R.id.camera_toggle_button);
        mute_button = (ImageButton) findViewById(R.id.mute_button);
        audio_device_icon = (ImageView) findViewById(R.id.audio_device_icon);
        misc_button = (ImageButton) findViewById(R.id.misc_button);
        misc_button_pad = (TextView) findViewById(R.id.misc_button_pad);

        if (PREF__window_security)
        {
            // prevent screenshots and also dont show the window content in recent activity screen
            initializeScreenshotSecurity(this);
        }

        trifa_is_MicrophoneMute = false;
        ca = this;

        sensor_manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximity_sensor = sensor_manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        callactivity_handler = new Handler(getMainLooper());
        callactivity_handler_s = callactivity_handler;

        // set volume control -------------
        //**//setVolumeControlStream(AudioManager.STREAM_MUSIC);
        System.out.println("AVCS:MUSIC:0");

        AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        try
        {
            //**//manager.setMode(AudioManager.MODE_NORMAL);
            // manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            manager.setSpeakerphoneOn(true);
            Callstate.audio_speaker = true;
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }

        // Drawable d2 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_headset).backgroundColor(Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        // audio_device_icon.setImageDrawable(null);
        audio_device_icon.setVisibility(View.VISIBLE);

        try
        {
            if (dh._Detect())
            {
                // headset plugged in
                Log.i(TAG, "onReceive:headset:plugged in");
                manager.setSpeakerphoneOn(false);
                manager.setWiredHeadsetOn(true);
                Callstate.audio_device = 1;
                Callstate.audio_speaker = false;
                update_audio_device_icon();
                manager.setBluetoothScoOn(false);
            }
            else
            {
                audio_device_icon.setImageDrawable(null);
                Log.i(TAG, "onReceive:headset:setImageDrawable:null1");
            }
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
            audio_device_icon.setImageDrawable(null);
            Log.i(TAG, "onReceive:headset:setImageDrawable:null2");
        }
        // set volume control -------------

        boolean mVisible = true;
        mContentView = (CustomVideoImageView) findViewById(R.id.video_view);

        calling_activity_top_viewgroup_vg = (ViewGroup) findViewById(R.id.calling_activity_top_viewgroup);

        caller_avatar_view = (ImageView) findViewById(R.id.caller_avatar_view);

        video_box_self_preview_01 = findViewById(R.id.video_box_self_preview_01);
        video_box_left_top_01 = findViewById(R.id.video_box_left_top_01);
        video_box_right_top_01 = findViewById(R.id.video_box_right_top_01);

        video_box_self_preview_01.setVisibility(View.INVISIBLE);
        video_box_left_top_01.setVisibility(View.INVISIBLE);
        video_box_right_top_01.setVisibility(View.INVISIBLE);

        volume_slider_seekbar_01 = (SeekBar) findViewById(R.id.volume_slider_seekbar);
        video_add_delay_slider_seekbar_01 = (SeekBar) findViewById(R.id.video_add_delay_slider_seekbar);
        video_add_delay_slider_infotext_01 = (TextView) findViewById(R.id.video_add_delay_slider_infotext);

        box_right_volumeslider_01 = (View) findViewById(R.id.video_box_right_volumeslider_01);
        box_right_volumeslider_01.setVisibility(View.VISIBLE);
        box_right_volumeslider_01.setAlpha(slider_alpha);

        volume_slider_seekbar_01.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:action:" + event.getAction());

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        box_right_volumeslider_01.setAlpha(slider_alpha);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                else if ((event.getAction() == MotionEvent.ACTION_DOWN) ||
                         (event.getAction() == MotionEvent.ACTION_CANCEL))
                {
                    try
                    {
                        box_right_volumeslider_01.setAlpha(1.0f);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                return false;
            }
        });

        volume_slider_seekbar_01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar s, int progress_value, boolean from_user)
            {
                if ((progress_value >= 0) && (progress_value <= 100))
                {
                    PREF__audio_play_volume_percent = progress_value;
                    try
                    {
                        set_audio_play_volume_percent(PREF__audio_play_volume_percent);
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                    try
                    {
                        SharedPreferences settings_cs2 = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());
                        settings_cs2.edit().putInt("audio_play_volume_percent",
                                                   PREF__audio_play_volume_percent).apply();
                        Log.i(TAG, "pref:set:PREF__audio_play_volume_percent=" + PREF__audio_play_volume_percent);
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                        Log.i(TAG, "pref:set:PREF__audio_play_volume_percent:EE:" + ee.getMessage());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });


        box_right_video_add_delay_slider_01 = (View) findViewById(R.id.video_box_right_video_add_delay_slider_01);
        box_right_video_add_delay_slider_01.setVisibility(View.VISIBLE);
        box_right_video_add_delay_slider_01.setAlpha(slider_alpha);


        video_add_delay_slider_seekbar_01.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:action:" + event.getAction());

                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        box_right_video_add_delay_slider_01.setAlpha(slider_alpha);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                else if ((event.getAction() == MotionEvent.ACTION_DOWN) ||
                         (event.getAction() == MotionEvent.ACTION_CANCEL))
                {
                    try
                    {
                        box_right_video_add_delay_slider_01.setAlpha(1.0f);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "volume_slider_seekbar_01.setOnTouchListener:touch:001:EE:" + e.getMessage());
                    }
                }
                return false;
            }
        });


        video_add_delay_slider_seekbar_01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar s, int progress_value, boolean from_user)
            {
                if ((progress_value >= 0) && (progress_value <= 100))
                {
                    try
                    {
                        PREF__video_play_delay_ms = progress_value * 12;
                        toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                         ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_ADD_DELAY_MS.value,
                                         PREF__video_play_delay_ms);
                        video_add_delay_slider_infotext_01.setText("Video Delay: " + PREF__video_play_delay_ms + " ms");
                        SharedPreferences settings_cs1 = PreferenceManager.getDefaultSharedPreferences(
                                getApplicationContext());
                        settings_cs1.edit().putInt("video_play_delay_ms", PREF__video_play_delay_ms).apply();
                        Log.i(TAG, "pref:set:PREF__video_play_delay_ms=" + PREF__video_play_delay_ms);
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                        Log.i(TAG, "pref:set:PREF__video_play_delay_ms:EE:" + ee.getMessage());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });


        try
        {
            final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_face).color(
                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(200);
            caller_avatar_view.setImageDrawable(d1);

            String fname = get_vfs_image_filename_friend_avatar(
                    tox_friend_by_public_key__wrapper(Callstate.friend_pubkey));

            if (fname != null)
            {
                put_vfs_image_on_imageview(this, caller_avatar_view, d1, fname);
            }
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        right_top_text_1 = (TextView) findViewById(R.id.right_top_text_1);
        right_top_text_1b = (TextView) findViewById(R.id.right_top_text_1b);
        right_top_text_2 = (TextView) findViewById(R.id.right_top_text_2);
        right_top_text_3 = (TextView) findViewById(R.id.right_top_text_3);
        right_top_text_4 = (TextView) findViewById(R.id.right_top_text_4);
        right_left_text_1 = (TextView) findViewById(R.id.right_left_text_1);
        quality_slider = (DiscreteSlider) findViewById(R.id.quality_slider);
        text_vq_low = (TextView) findViewById(R.id.text_vq_low);
        text_vq_med = (TextView) findViewById(R.id.text_vq_med);
        text_vq_high = (TextView) findViewById(R.id.text_vq_high);


        text_vq_low.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    VIDEO_ENCODER_MAX_BITRATE_LOW);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_LOW);
                        if (res != 0)
                        {
                            quality_slider_position = 0;
                            quality_slider.setPosition(quality_slider_position);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });


        text_vq_med.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    VIDEO_ENCODER_MAX_BITRATE_MED);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_MED);
                        if (res != 0)
                        {
                            quality_slider_position = 1;
                            quality_slider.setPosition(quality_slider_position);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        text_vq_high.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    VIDEO_ENCODER_MAX_BITRATE_HIGH);

                        int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                   ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                                   VIDEO_ENCODER_MAX_QUANTIZER_HIGH);
                        if (res != 0)
                        {
                            quality_slider_position = 2;
                            quality_slider.setPosition(quality_slider_position);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "text_vq_low:touch:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });


        update_bitrates();
        update_fps();
        update_call_time();
        set_video_delay_ms();
        set_audio_play_volume();

        quality_slider.setPosition(quality_slider_position);

        // Detect when slider position changes
        quality_slider.setOnDiscreteSliderChangeListener(new DiscreteSlider.OnDiscreteSliderChangeListener()
        {
            @Override
            public void onPositionChanged(int position)
            {
                Log.i(TAG, "setOnDiscreteSliderChangeListener:pos=" + position);
                final int prev_position = quality_slider_position;

                if (prev_position != position)
                {
                    int value = VIDEO_ENCODER_MAX_QUANTIZER_LOW;
                    int value1 = VIDEO_ENCODER_MAX_BITRATE_LOW;
                    if (position == 1)
                    {
                        value = VIDEO_ENCODER_MAX_QUANTIZER_MED;
                        value1 = VIDEO_ENCODER_MAX_BITRATE_MED;
                    }
                    else if (position == 2)
                    {
                        value = VIDEO_ENCODER_MAX_QUANTIZER_HIGH;
                        value1 = VIDEO_ENCODER_MAX_BITRATE_HIGH;
                    }
                    int res1 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                value1);

                    int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                               ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_RC_MAX_QUANTIZER.value,
                                               value);
                    Log.i(TAG, "setOnDiscreteSliderChangeListener:res:" + res);

                    if (res != 0)
                    {
                        quality_slider_position = position;
                        Log.i(TAG, "setOnDiscreteSliderChangeListener:pos_NEW:" + quality_slider.getPosition());
                    }
                    else
                    {
                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(100);
                                    // set slide back to prev. position
                                    quality_slider.setPosition(prev_position);
                                    Log.i(TAG, "setOnDiscreteSliderChangeListener:pos_revert:" +
                                               quality_slider.getPosition());
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "setOnDiscreteSliderChangeListener:001:EE:" + e.getMessage());
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        });

        if (PREF__X_misc_button_enabled)
        {
            misc_button.setVisibility(View.VISIBLE);
            misc_button_pad.setVisibility(View.VISIBLE);

            Drawable d8 = new IconicsDrawable(this).
                    icon(GoogleMaterial.Icon.gmd_touch_app).
                    backgroundColor(Color.TRANSPARENT).color(getResources().
                    getColor(R.color.colorPrimaryDark)).sizeDp(50);
            misc_button.setImageDrawable(d8);

            misc_button.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                    {
                        try
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).
                                    icon(GoogleMaterial.Icon.gmd_touch_app).
                                    backgroundColor(Color.TRANSPARENT).color(getResources().
                                    getColor(R.color.md_green_600)).sizeDp(50);
                            misc_button.setImageDrawable(d1a);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try
                        {
                            Drawable d2a = new IconicsDrawable(v.getContext()).
                                    icon(GoogleMaterial.Icon.gmd_touch_app).
                                    backgroundColor(Color.TRANSPARENT).color(getResources().
                                    getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            misc_button.setImageDrawable(d2a);

                            // send misc. message to friend, and do NOT save to DB
                            // TODO: use new functions to send the message
                            //
                            //MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                            //        tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), 0,
                            //        PREF__X_misc_button_msg);
                            //long res = result.msg_num;
                            //Log.i(TAG, "tox_friend_send_message_wrapper:result=" + res);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });


        }
        else
        {
            misc_button.setVisibility(View.GONE);
            misc_button_pad.setVisibility(View.GONE);
        }

        final Drawable d1 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_mic).backgroundColor(
                Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        mute_button.setImageDrawable(d1);
        mute_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    try
                    {
                        // if (audio_manager_s.isMicrophoneMute())
                        if (trifa_is_MicrophoneMute)
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
                        }
                        else
                        {
                            Drawable d1a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.md_green_600)).sizeDp(50);
                            mute_button.setImageDrawable(d1a);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    try
                    {
                        // if (audio_manager_s.isMicrophoneMute())
                        if (trifa_is_MicrophoneMute)
                        {
                            audio_manager_s.setMicrophoneMute(false);
                            trifa_is_MicrophoneMute = false;
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                        }
                        else
                        {
                            audio_manager_s.setMicrophoneMute(true);
                            trifa_is_MicrophoneMute = true;
                            Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                    GoogleMaterial.Icon.gmd_mic_off).backgroundColor(Color.TRANSPARENT).color(
                                    getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
                            mute_button.setImageDrawable(d2a);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "setMicrophoneMute:001:EE:" + e.getMessage());
                    }
                }
                return true;
            }
        });

        // on startup always use front camera
        active_camera_type = FRONT_CAMERA_USED;

        final Drawable d3 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_highlight_off).backgroundColor(
                Color.TRANSPARENT).color(Color.parseColor("#A0FF0000")).sizeDp(50);
        decline_button.setImageDrawable(d3);
        // #AARRGGBB

        final Drawable d4 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_check_circle).backgroundColor(
                Color.TRANSPARENT).color(Color.parseColor("#EF088A29")).sizeDp(50);
        accept_button.setImageDrawable(d4);

        if (Callstate.accepted_call == 1)
        {
            // HINT: also when starting a call ourselves!!
            caller_avatar_view.setVisibility(View.VISIBLE);
            accept_button.setVisibility(View.GONE);
            camera_toggle_button.setVisibility(View.VISIBLE);
            mute_button.setVisibility(View.VISIBLE);

            stop_ringtone();
        }
        else
        {
            caller_avatar_view.setVisibility(View.VISIBLE);
            accept_button.setVisibility(View.VISIBLE);
            camera_toggle_button.setVisibility(View.GONE);
            mute_button.setVisibility(View.GONE);

            start_ringtone();
        }

        if (active_camera_type == FRONT_CAMERA_USED)
        {
            final Drawable d5 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_front).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            camera_toggle_button.setImageDrawable(d5);
            Log.i(TAG, "active_camera_type(5)=" + active_camera_type);
        }
        else
        {
            final Drawable d6 = new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(
                    Color.TRANSPARENT).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
            camera_toggle_button.setImageDrawable(d6);
            Log.i(TAG, "active_camera_type(6)=" + active_camera_type);
        }

        camera_toggle_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    Log.i(TAG, "active_camera_type(7)=" + active_camera_type);

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.md_green_600)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                }
                else
                {
                    Log.i(TAG, "active_camera_type(8)=" + active_camera_type);

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_rear).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }
                    else
                    {
                        Drawable d2a = new IconicsDrawable(v.getContext()).icon(
                                GoogleMaterial.Icon.gmd_camera_front).backgroundColor(Color.TRANSPARENT).color(
                                getResources().getColor(R.color.colorPrimaryDark)).sizeDp(7);
                        camera_toggle_button.setImageDrawable(d2a);
                    }

                    final Thread toggle_thread = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(20);
                            }
                            catch (Exception e)
                            {
                                // e.printStackTrace();
                            }
                            toggle_camera();
                        }
                    };
                    toggle_thread.start();
                }

                return true;
            }
        });

        initUI();
        initViewParams();

        top_text_line_str1 = Callstate.friend_alias_name;
        top_text_line_str2 = "";
        top_text_line_str3 = "";
        top_text_line_str4 = "";
        update_top_text_line();

        accept_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    if (event.getAction() != MotionEvent.ACTION_UP)
                    {
                    }
                    else
                    {
                        if (Callstate.accepted_call != 1)
                        {
                            Callstate.accepted_call = 1;

                            Log.i(TAG, "answer button pressed");
                            toxav_answer(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                         GLOBAL_AUDIO_BITRATE,
                                         GLOBAL_VIDEO_BITRATE); // these 2 bitrate values are very strange!! sometimes no video incoming!!
                            // need to set our state manually here, no callback from toxcore :-(
                            Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_SENDING_V.value;
                            // need to set our state manually here, no callback from toxcore :-(

                            caller_avatar_view.setVisibility(View.GONE);
                            accept_button.setVisibility(View.GONE);
                            camera_toggle_button.setVisibility(View.VISIBLE);
                            mute_button.setVisibility(View.VISIBLE);

                            Callstate.call_start_timestamp = System.currentTimeMillis();
                            String a = "" +
                                       (int) ((Callstate.call_start_timestamp - Callstate.call_init_timestamp) / 1000) +
                                       "s";
                            top_text_line_str2 = a;
                            update_top_text_line();

                            Log.i(TAG, "on_call_started_actions:01");
                            on_call_started_actions();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });

        decline_button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                try
                {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        Log.i(TAG, "decline button pressed");
                        toxav_call_control(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                           ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
                        on_call_ended_actions();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });

        Log.i(TAG, "onCreate:99");

    }

    public static void set_av_latency()
    {
        try
        {
            int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                       ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value, 2);
            Log.i(TAG, "decoder buffer set to ms=" + 2 + ":res=" + res);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "max_v_birate_set:EE:" + e.getMessage());
        }
    }

    public static void set_max_video_bitrate()
    {
        try
        {
            int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                       ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                       VIDEO_ENCODER_MAX_BITRATE_LOW);
            Log.i(TAG, "max_v_birate_set:res=" + res);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "max_v_birate_set:EE:" + e.getMessage());
        }
    }

    public static void close_calling_activity()
    {
        Callstate.reset_values();
        // close calling activity --------
        ca.finish();
        // close calling activity --------
    }

    synchronized public static void update_top_text_line()
    {
        Log.i(TAG, "update_top_text_line(1):top_text_line_str3=" + top_text_line_str3);
        update_top_text_line(top_text_line_str3, 3);
    }

    synchronized public static void update_top_text_line(String text2, int linenum)
    {
        Log.i(TAG, "update_top_text_line(2):str=" + text2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str1=" + top_text_line_str1);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str2=" + top_text_line_str2);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str3=" + top_text_line_str3);
        Log.i(TAG, "update_top_text_line(2):top_text_line_str4=" + top_text_line_str4);

        if (linenum == 3)
        {
            top_text_line_str3 = text2;
        }
        else if (linenum == 4)
        {
            top_text_line_str4 = text2;
        }

        Log.i(TAG, "update_top_text_line(2b):top_text_line_str3=" + top_text_line_str3);

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "update_top_text_line(2c):top_text_line_str3=" + top_text_line_str3);

                    if ((top_text_line_str3 != "") || (top_text_line_str4 != ""))
                    {
                        top_text_line.setText(
                                top_text_line_str1 + ":" + top_text_line_str2 + ":" + top_text_line_str3 + ":" +
                                top_text_line_str4);
                    }
                    else
                    {
                        if (top_text_line_str2 != "")
                        {
                            top_text_line.setText(top_text_line_str1 + ":" + top_text_line_str2);
                        }
                        else
                        {
                            top_text_line.setText(top_text_line_str1);
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    public static void initializeScreenshotSecurity(Activity a)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        else
        {
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    public void onBackPressed()
    {
        // dont let the user use the back button to close the activity
    }


    //    private final Runnable mHidePart2Runnable = new Runnable()
    //    {
    //        @SuppressLint("InlinedApi")
    //        @Override
    //        public void run()
    //        {
    //            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    //        }
    //    };

    //    private final Runnable mShowPart2Runnable = new Runnable()
    //    {
    //        @Override
    //        public void run()
    //        {
    //            ActionBar actionBar = getSupportActionBar();
    //            if (actionBar != null)
    //            {
    //                actionBar.show();
    //            }
    //        }
    //    };

    //    private final Runnable mHideRunnable = new Runnable()
    //    {
    //        @Override
    //        public void run()
    //        {
    //            hide();
    //        }
    //    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // delayedHide(100);
    }

    //    private void toggle()
    //    {
    //        if (mVisible)
    //        {
    //            hide();
    //        }
    //        else
    //        {
    //            show();
    //        }
    //    }

    //    private void hide()
    //    {
    //        // Hide UI first
    //        ActionBar actionBar = getSupportActionBar();
    //        if (actionBar != null)
    //        {
    //            actionBar.hide();
    //        }
    //        mVisible = false;
    //
    //        // Schedule a runnable to remove the status and navigation bar after a delay
    //        mHideHandler.removeCallbacks(mShowPart2Runnable);
    //        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    //    }

    //    @SuppressLint("InlinedApi")
    //    private void show()
    //    {
    //        // Show the system bar
    //        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    //        mVisible = true;
    //
    //        // Schedule a runnable to display UI elements after a delay
    //        mHideHandler.removeCallbacks(mHidePart2Runnable);
    //        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    //    }

    //    private void delayedHide(int delayMillis)
    //    {
    //        mHideHandler.removeCallbacks(mHideRunnable);
    //        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    //    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume:01");

        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            if (PREF__use_H264_hw_encoding)
            {
                prepareEncoder();
                // prepareDecoder();
            }
        }
        activity_state = 1;

        sensor_manager.registerListener(this, proximity_sensor, SensorManager.SENSOR_DELAY_NORMAL);

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
                audio_thread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioReceiver.stopped)
            {
                AudioReceiver.close();
                audio_receiver_thread.join();
                audio_receiver_thread = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (AudioReceiver.stopped)
            {
                audio_receiver_thread = new AudioReceiver();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (AudioRecording.stopped)
            {
                audio_thread = new AudioRecording();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // update call time every second -----------
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {
                // Log.i(TAG, "update_call_time -> call");
                update_call_time();
                if (activity_state != 0)
                {
                    ha.postDelayed(this, 1000);
                }
            }
        }, 1000);
        // update call time every second -----------

        Log.i(TAG, "onResume:99");
    }

    void toggle_camera()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CameraWrapper.getInstance().doStopCamera();

                    if (active_camera_type == FRONT_CAMERA_USED)
                    {
                        CameraWrapper.camera_preview_size2 = null;
                        active_camera_type = BACK_CAMERA_USED;
                        Log.i(TAG, "active_camera_type(8a)=" + active_camera_type);
                        CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, false);
                    }
                    else
                    {
                        CameraWrapper.camera_preview_size2 = null;
                        active_camera_type = FRONT_CAMERA_USED;
                        Log.i(TAG, "active_camera_type(8b)=" + active_camera_type);
                        CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);
                    }
                }
                catch (Exception e)
                {
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    // -------------------------------------------------------
    // TODO
    // this is a bad workaround to really show the cam preview
    // -------------------------------------------------------
    static void reinit_camera(CallingActivity c)
    {
        try
        {
            Log.i(TAG, "active_camera_type(1)=" + active_camera_type);
            CameraWrapper.getInstance().doStopCamera();
            Log.i(TAG, "active_camera_type(2)=" + active_camera_type);
            CameraWrapper.camera_preview_size2 = null;
            Log.i(TAG, "active_camera_type(3)=" + active_camera_type);
            CameraWrapper.getInstance().doOpenCamera(c, true);
            Log.i(TAG, "active_camera_type(4)=" + active_camera_type);
        }
        catch (Exception e)
        {
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            if (PREF__use_H264_hw_encoding)
            {
                releaseEncoder();
                // releaseDecoder_h264();
            }
        }
        sensor_manager.unregisterListener(this);
        activity_state = 0;

        stop_ringtone();

        try
        {
            if (wl1 != null)
            {
                if (wl1.isHeld())
                {
                    wl1.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            if (wl2 != null)
            {
                if (wl2.isHeld())
                {
                    wl2.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioRecording.stopped)
            {
                AudioRecording.close();
                audio_thread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!AudioReceiver.stopped)
            {
                AudioReceiver.close();
                audio_receiver_thread.join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            // HINT: of we should for any reason leave the callscreen activity, end any active call
            // TODO: make this nicer, let the user return to an active call
            toxav_call_control(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                               ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        on_call_ended_actions();
    }

    // ---------------
    @Override
    protected void onStart()
    {
        Log.i(TAG, "onStart:01");
        super.onStart();

        Thread openThread = new Thread()
        {
            @Override
            public void run()
            {
                active_camera_type = FRONT_CAMERA_USED;
                Log.i(TAG, "active_camera_type(01)=" + active_camera_type);
                CameraWrapper.camera_preview_size2 = null;
                CameraWrapper.getInstance().doOpenCamera(CallingActivity.this, true);
            }
        };
        openThread.start();

        Log.i(TAG, "onStart:99");
    }

    private void initUI()
    {
        cameraSurfacePreview = (CameraSurfacePreview) findViewById(R.id.camera_surfaceview);
    }

    private void initViewParams()
    {
        ViewGroup.LayoutParams params = cameraSurfacePreview.getLayoutParams();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        params.width = screenWidth;
        params.height = screenHeight;
        mPreviewRate = (float) screenHeight / (float) screenWidth;
        cameraSurfacePreview.setLayoutParams(params);
    }

    @Override
    public void cameraHasOpened()
    {
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Log.i(TAG, "cameraHasOpened:**************** CAMERA OPEN ****************");
        Callstate.camera_opened = true;
        SurfaceHolder holder = cameraSurfacePreview.getSurfaceHolder();
        CameraWrapper.getInstance().doStartPreview(holder, mPreviewRate);
    }


    public void turnOnScreen()
    {
        if (PREF__allow_screen_off_in_audio_call)
        {
            turnOnScreen__old();
        }
        else
        {
            turnOnScreen__new();
        }
    }

    public void turnOffScreen()
    {
        if (PREF__allow_screen_off_in_audio_call)
        {
            turnOffScreen__old();
        }
        else
        {
            turnOffScreen__new();
        }
    }

    public void turnOnScreen__new()
    {
        try
        {
            // calling_activity_top_viewgroup_vg.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turn*ON*Screen");
        Callstate.other_video_enabled = 1;
        Callstate.my_video_enabled = 1;
    }

    public void turnOffScreen__new()
    {
        try
        {
            // in case the phone does not really turn the screen off
            mContentView.setVisibility(View.INVISIBLE);
            // calling_activity_top_viewgroup_vg.setVisibility(View.INVISIBLE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turnOffScreen");
        Callstate.other_video_enabled = 0;
        Callstate.my_video_enabled = 0;
    }


    public void turnOnScreen__old()
    {
        mContentView.setVisibility(View.VISIBLE);

        // turn on screen
        try
        {
            if (wl2 != null)
            {
                if (wl2.isHeld())
                {
                    wl2.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "turn*ON*Screen");
        Callstate.other_video_enabled = 1;
        Callstate.my_video_enabled = 1;

        if (wl1 == null)
        {
            wl1 = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                 "trifa:trifa_screen_on");
        }

        try
        {
            if (wl1 != null)
            {
                if (!wl1.isHeld())
                {
                    wl1.acquire();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @TargetApi(21)
    public void turnOffScreen__old()
    {
        try
        {
            if (wl1 != null)
            {
                if (wl1.isHeld())
                {
                    wl1.release();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        Log.i(TAG, "turnOffScreen");
        Callstate.other_video_enabled = 0;
        Callstate.my_video_enabled = 0;

        // turn off screen
        if (wl2 == null)
        {
            wl2 = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "trifa:trifa_screen_OFF");
        }


        try
        {
            if (wl2 != null)
            {
                if (!wl2.isHeld())
                {
                    wl2.acquire();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        // in case the phone does not really turn the screen off
        mContentView.setVisibility(View.INVISIBLE);
    }


    private void requestAudioFocus()
    {
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        //        {
        //            AudioAttributes playbackAttributes = new AudioAttributes.Builder().setUsage(
        //                    AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(
        //                    AudioAttributes.CONTENT_TYPE_SPEECH).build();
        //            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(
        //                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(
        //                    playbackAttributes).setAcceptsDelayedFocusGain(true).setOnAudioFocusChangeListener(
        //                    new AudioManager.OnAudioFocusChangeListener()
        //                    {
        //                        @Override
        //                        public void onAudioFocusChange(int i)
        //                        {
        //                        }
        //                    }).build();
        //            audio_manager_s.requestAudioFocus(focusRequest);
        //        }
        //        else
        //        {
        //            audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
        //                                              AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY)
        {
            // Log.i(TAG, "onSensorChanged:value=" + event.values[0] + " max=" + proximity_sensor.getMaximumRange());

            if (event.values[0] < proximity_sensor.getMaximumRange())
            {
                // close to ear
                if (Callstate.audio_speaker == true)
                {
                    Log.i(TAG, "onSensorChanged:--> EAR");

                    set_filteraudio_active(0);

                    Callstate.audio_speaker = false;

                    //audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    //                                  AudioManager.AUDIOFOCUS_GAIN);
                    //**// requestAudioFocus();

                    try
                    {
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        // audioManager.setMode(AudioManager.MODE_IN_CALL);
                        // audioManager.setMode(AudioManager.MODE_NORMAL);
                        // audio_manager_s.setMode(AudioManager.MODE_NORMAL);
                        Log.i(TAG, "onSensorChanged:setMode(AudioManager.MODE_IN_COMMUNICATION)");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "onSensorChanged:EE1:" + e.getMessage());
                    }

                    try
                    {
                        if (!dh._Detect())
                        {
                            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            audioManager.setSpeakerphoneOn(false);

                            //                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            //                            {
                            //                                Log.i(TAG, "onSensorChanged:isStreamMute(STREAM_MUSIC)=" +
                            //                                           audio_manager_s.isStreamMute(AudioManager.STREAM_MUSIC));
                            //                                Log.i(TAG, "onSensorChanged:isStreamMute(STREAM_VOICE_CALL)=" +
                            //                                           audio_manager_s.isStreamMute(AudioManager.STREAM_VOICE_CALL));
                            //                            }
                            Log.i(TAG, "onSensorChanged:setSpeakerphoneOn(false)");
                        }

                        try
                        {
                            turnOffScreen();
                            Log.i(TAG, "onSensorChanged:turnOffScreen()");
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                            Log.i(TAG, "onSensorChanged:EE2:" + e2.getMessage());
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Callstate.audio_speaker = true;
                        Log.i(TAG, "onSensorChanged:audio_speaker = true");
                    }

                    try
                    {
                        // set volume control -------------
                        // setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                        // HINT: this seems to be correct now? at least the on devices I tested on
                        //**//setVolumeControlStream(AudioManager.STREAM_MUSIC);

                        //                        Class audioSystemClass = Class.forName("android.media.AudioManager");
                        //                        Method setForceUse = audioSystemClass.getMethod("forceVolumeControlStream", int.class);
                        //                        setForceUse.invoke(audio_manager_s, AudioManager.STREAM_MUSIC);

                        System.out.println("AVCS:VOICE:1");
                        // set volume control -------------
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "onSensorChanged:EE3:" + e.getMessage());
                    }
                }
            }
            else
            {
                // away from ear
                if (Callstate.audio_speaker == false)
                {
                    Log.i(TAG, "onSensorChanged:--> speaker");
                    if (PREF__use_software_aec)
                    {
                        set_filteraudio_active(1);
                    }
                    else
                    {
                        set_filteraudio_active(0);
                    }

                    Callstate.audio_speaker = true;

                    // audio_manager_s.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                    //                                  AudioManager.AUDIOFOCUS_GAIN);
                    //**// requestAudioFocus();

                    try
                    {
                        audio_manager_s.setMode(AudioManager.MODE_NORMAL);
                        // audio_manager_s.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        Log.i(TAG, "onSensorChanged:setMode(AudioManager.MODE_NORMAL)");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "onSensorChanged:EE4:" + e.getMessage());
                    }

                    try
                    {
                        if (!dh._Detect())
                        {
                            audio_manager_s.setSpeakerphoneOn(true);
                            Log.i(TAG, "onSensorChanged:setSpeakerphoneOn(true)");
                        }

                        try
                        {
                            turnOnScreen();
                            Log.i(TAG, "onSensorChanged:turnOnScreen()");
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                            Log.i(TAG, "onSensorChanged:EE5:" + e2.getMessage());
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Callstate.audio_speaker = false;
                        Log.i(TAG, "onSensorChanged:audio_speaker = false");
                        Log.i(TAG, "onSensorChanged:EE6:" + e.getMessage());
                    }

                    //                    try
                    //                    {
                    //                        // set volume control -------------
                    //                        setVolumeControlStream(AudioManager.STREAM_MUSIC);
                    //                        System.out.println("AVCS:VOICE:2");
                    //                        // set volume control -------------
                    //                    }
                    //                    catch (Exception e)
                    //                    {
                    //                        e.printStackTrace();
                    //                        Log.i(TAG, "onSensorChanged:EE7:" + e.getMessage());
                    //                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    void update_call_time()
    {
        if (Callstate.accepted_call == 1)
        {
            if (Callstate.call_start_timestamp != -1)
            {

                right_left_text_1.setText(format_timeduration_from_seconds(
                        (System.currentTimeMillis() - Callstate.call_start_timestamp) / 1000));
            }
            else
            {
                right_left_text_1.setText("...");
            }
        }
        else
        {
            right_left_text_1.setText("...");
        }
    }

    static void update_audio_device_icon()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "update_audio_device_icon:enter");

                    if (Callstate.audio_device == 0)
                    {
                        Log.i(TAG, "update_audio_device_icon:clear");
                        audio_device_icon.setImageDrawable(null);
                    }
                    else if (Callstate.audio_device == 1)
                    {
                        Log.i(TAG, "update_audio_device_icon:headset");
                        Drawable d4 = new IconicsDrawable(ca).icon(GoogleMaterial.Icon.gmd_headset).backgroundColor(
                                Color.TRANSPARENT).color(ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(
                                80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else if (Callstate.audio_device == 2)
                    {
                        Log.i(TAG, "update_audio_device_icon:bluetooth");
                        Drawable d4 = new IconicsDrawable(ca).icon(
                                GoogleMaterial.Icon.gmd_bluetooth_audio).backgroundColor(Color.TRANSPARENT).color(
                                ca.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(80);
                        audio_device_icon.setImageDrawable(d4);
                    }
                    else // audio_device == ??
                    {
                        Log.i(TAG, "update_audio_device_icon:null");
                        audio_device_icon.setImageDrawable(null);
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "update_audio_device_icon:EE:" + e.getMessage());
                }
            }
        };
        callactivity_handler_s.post(myRunnable);
    }

    static void set_video_delay_ms()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                     ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_ADD_DELAY_MS.value,
                                     PREF__video_play_delay_ms);
                    video_add_delay_slider_infotext_01.setText("Video Delay: " + PREF__video_play_delay_ms + " ms");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        video_add_delay_slider_seekbar_01.setProgress((PREF__video_play_delay_ms / 12), true);
                    }
                    else
                    {
                        video_add_delay_slider_seekbar_01.setProgress((PREF__video_play_delay_ms / 12));
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    static void set_audio_play_volume()
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        volume_slider_seekbar_01.setProgress(PREF__audio_play_volume_percent, true);
                    }
                    else
                    {
                        volume_slider_seekbar_01.setProgress(PREF__audio_play_volume_percent);
                    }
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

                try
                {
                    set_audio_play_volume_percent(PREF__audio_play_volume_percent);
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    // actions to take when a call starts by:
    // a) accepting an incoming call
    // b) the other party accepting our call invitation
    static void on_call_started_actions()
    {
        set_max_video_bitrate();
        set_av_latency();
        set_video_delay_ms();
        set_audio_play_volume();
        stop_ringtone();

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CallingActivity.video_box_self_preview_01.setVisibility(View.VISIBLE);
                    CallingActivity.video_box_left_top_01.setVisibility(View.VISIBLE);
                    CallingActivity.video_box_right_top_01.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };
        CallingActivity.callactivity_handler_s.post(myRunnable);
    }

    // actions to take when a call ends by:
    // a) us ending the call
    // b) the other party ending the call
    static void on_call_ended_actions()
    {
        try
        {
            close_calling_activity();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------

    /* Use this method to provide YUV420 buffers for encoding */
    static class h264_encoder_output_data
    {
        byte[] data;
        byte[] sps_pps;
    }

    public static void feed_h264_encoder(byte[] buf, int frame_width_px, int frame_height_px)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            int v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_LOW * 1000;

            if (quality_slider_position == 2)
            {
                v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_HIGH * 1000;
            }
            else if (quality_slider_position == 1)
            {
                v_bitrate_bps = VIDEO_ENCODER_MAX_BITRATE_MED * 1000;
            }

            reconfigure_h264_encoder(v_bitrate_bps, frame_width_px, frame_height_px);

            /* Find an unallocated input buffer to store food */
            try
            {
                int inputBufferIndex = mEncoder.dequeueInputBuffer(
                        500); // This method will return immediately if timeoutUs == 0
                if (inputBufferIndex >= 0)
                {
                    /* Get input buffer and fill it with our input */
                    ByteBuffer inputBuffer = mEncoder.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(buf);
                    /* Enqueue buffer */
                    // Log.d(TAG, "feed_h264_encoder:Enqueued input index: " + inputBufferIndex);

                    long ptsUsec = computePresentationTime();
                    mEncoder.queueInputBuffer(inputBufferIndex, 0, buf.length, ptsUsec, 0);
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "feed_h264_encoder:Get free buffer failed");
            }
        }
        else
        {
            // TODO: do something here?
        }
    }

    public static h264_encoder_output_data fetch_from_h264_encoder()
    {
        h264_encoder_output_data ret = new h264_encoder_output_data();
        ret.data = null;
        ret.sps_pps = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int encoderStatus = MediaCodec.INFO_TRY_AGAIN_LATER;

            try
            {
                if (mEncoder == null)
                {
                    return ret;
                }
                encoderStatus = mEncoder.dequeueOutputBuffer(info,
                                                             1000); // Dequeue an output buffer, block at most "timeoutUs" microseconds.
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                return ret;
            }

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                // no output available yet
                // Log.d(TAG, "fetch_from_h264_encoder:no output from encoder available");
                return ret;
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // not expected for an encoder
                Log.d(TAG, "fetch_from_h264_encoder:old_format: " + video_encoder_format);
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "fetch_from_h264_encoder:new_format: " + newFormat);
                ByteBuffer csd0 = newFormat.getByteBuffer("csd-0");
                ByteBuffer csd1 = newFormat.getByteBuffer("csd-1");
                System.out.println("fetch_from_h264_encoder:csd-0:len=" + csd0.limit());
                System.out.println("fetch_from_h264_encoder:csd-1:len=" + csd1.limit());

                video_encoder_format = newFormat;

                return ret;
            }
            else if (encoderStatus < 0)
            {
                // Log.d(TAG,
                //      "fetch_from_h264_encoder:unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                return ret;
            }
            else // encoderStatus >= 0
            {
                /* Compressed frame is ready! */
                ByteBuffer compressed = mEncoder.getOutputBuffer(encoderStatus);
                if (compressed == null)
                {
                    // Log.d(TAG, "fetch_from_h264_encoder:encoderOutputBuffer " + encoderStatus + " was null");
                    return ret;
                }
                else
                {
                    // Log.d(TAG, "fetch_from_h264_encoder:Dequeue output index: " + encoderStatus);
                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    compressed.position(info.offset);
                    compressed.limit(info.offset + info.size);
                    /* Copy to byte array for further processing */
                    byte[] arr = new byte[compressed.remaining()];
                    compressed.get(arr);
                    compressed.position(info.offset);

                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                    {
                        // Codec config info.  Only expected on first packet.  One way to
                        // handle this is to manually stuff the data into the MediaFormat
                        // and pass that to configure().  We do that here to exercise the API.
                        //**// MediaFormat video_encoder_format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
                        //**// video_encoder_format.setByteBuffer("csd-0", compressed);
                        //
                        // --> "csd-0" or SPS/PPS data
                        //
                        ret.sps_pps = arr;
                        System.out.println(
                                "fetch_from_h264_encoder:SPS_PPS:len=" + arr.length + " data=" + arr.toString());
                    }
                    else
                    {
                        // Get a decoder input buffer, blocking until it's available.
                    }

                    /* Release MediaCodec buffer */
                    mEncoder.releaseOutputBuffer(encoderStatus, false);
                    ret.data = arr;
                    return ret;
                }
            }
        }
        else
        {
            // TODO: do something here?
        }

        return ret;
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private static long computePresentationTime()
    {
        // Log.i(TAG, "computePresentationTime:" + (System.currentTimeMillis() * 1000));
        return (System.currentTimeMillis() * 1000);
    }

    static void reconfigure_h264_encoder(int bitrate_bits_per_second, int width, int height)
    {
        if ((video_encoder_width != width) || (video_encoder_height != height) ||
            (v_bitrate_bits_per_second != bitrate_bits_per_second))
        {
            releaseEncoder();
            video_encoder_width = width;
            video_encoder_height = height;
            v_bitrate_bits_per_second = bitrate_bits_per_second;
            Log.d(TAG, "reconfigure_h264_encoder:vbrate: " + v_bitrate_bits_per_second);
            prepareEncoder();
        }
        // else
        // {
        //     if (v_bitrate_bits_per_second != bitrate_bits_per_second)
        //     {
        //         v_bitrate_bits_per_second = bitrate_bits_per_second;
        //         video_encoder_format.setInteger(MediaFormat.KEY_BIT_RATE, v_bitrate_bits_per_second);
        //     }
        // }
    }

    /**
     * Configures the H264 encoder
     */
    static void prepareEncoder()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {

            /**
             {
             mime=video/avc,
             width=480,
             height=640,
             bitrate=120000,
             max-bitrate=120000,
             frame-rate=25,
             opaque_handle=0,
             csd-0=java.nio.HeapByteBuffer[pos=0 lim=13 cap=13]
             csd-1=java.nio.HeapByteBuffer[pos=0 lim=8 cap=8],
             vendor.rtc-ext-enc-base-layer-pid=0,
             vendor.rtc-ext-enc-bitrate-mode=120000,
             vendor.rtc-ext-enc-caps-ltr.max-count=4,
             vendor.rtc-ext-enc-caps-preprocess.max-downscale-factor=0,
             vendor.rtc-ext-enc-caps-preprocess.rotation=0,
             vendor.rtc-ext-enc-caps-temporal-layers.max-b-count=6,
             vendor.rtc-ext-enc-caps-temporal-layers.max-p-count=7,
             vendor.rtc-ext-enc-caps-vt-driver-version.number=180315
             vendor.rtc-ext-enc-custom-profile-level.level=1,
             vendor.rtc-ext-enc-custom-profile-level.profile=1,
             vendor.rtc-ext-enc-low-latency.enable=0,
             vendor.rtc-ext-enc-ltr-count.num-ltr-frames=0,
             vendor.rtc-ext-enc-sar.height=0,
             vendor.rtc-ext-enc-sar.width=0,
             vendor.rtc-ext-enc-slice.spacing=0,
             vendor.sec-ext-enc-qp-range.B-maxQP=50,
             vendor.sec-ext-enc-qp-range.B-minQP=5,
             vendor.sec-ext-enc-qp-range.I-maxQP=50,
             vendor.sec-ext-enc-qp-range.I-minQP=5,
             vendor.sec-ext-enc-qp-range.P-maxQP=50,
             vendor.sec-ext-enc-qp-range.P-minQP=5,
             }
             *
             */

            mBufferInfo = new MediaCodec.BufferInfo();
            video_encoder_format = MediaFormat.createVideoFormat(MIME_TYPE, video_encoder_width, video_encoder_height);
            video_encoder_format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                                            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            // video_encoder_format.setInteger(MediaFormat.KEY_COLOR_STANDARD, COLOR_STANDARD_BT601_PAL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_PRIORITY, 0); // 0: realtime priority
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_LATENCY, 0);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                video_encoder_format.setInteger(MediaFormat.KEY_OPERATING_RATE, 25);
            }
            // -----------------------------------------------------------------------------
            // HINT: https://stackoverflow.com/questions/21284874/illegal-state-exception-when-calling-mediacodec-configure
            video_encoder_format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
            //
            // -----------------------------------------------------------------------------

            // -- vendor extensions --
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-low-latency.enable",1);
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-caps-temporal-layers.max-b-count",1);
            // video_encoder_format.setInteger("vendor.rtc-ext-enc-caps-temporal-layers.max-p-count",1);
            // -- vendor extensions --

            video_encoder_format.setInteger(MediaFormat.KEY_BIT_RATE, v_bitrate_bits_per_second);
            video_encoder_format.setInteger(MediaFormat.KEY_BITRATE_MODE,
                                            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            video_encoder_format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            video_encoder_format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            // video_encoder_format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
            // video_encoder_format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);

            Log.d(TAG, "prepareEncoder:video_encoder_format: " + video_encoder_format);

            try
            {
                mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
                Log.d(TAG, "prepareEncoder:SUCCESS: " + mEncoder.getCodecInfo());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG, "prepareEncoder:EE1: " + e.getMessage());
            }
            mEncoder.configure(video_encoder_format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();

            // feed 2 dummy frame for less latency ----------------------------------------
            // int size_dummy = (video_encoder_width * video_encoder_width) * 3 / 2;
            // byte[] buf_dummy = new byte[size_dummy];
            // feed_h264_encoder(buf_dummy, video_encoder_width, video_encoder_height);
            // feed_h264_encoder(buf_dummy, video_encoder_width, video_encoder_height);
            // feed 2 dummy frame for less latency ----------------------------------------
        }
    }

    /**
     * Extracts all pending data from the encoder.
     */
    private static void drainEncoder()
    {
        final int TIMEOUT_USEC = 10000;
        Log.d(TAG, "drainEncoder:start");

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true)
        {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                break;      // out of while
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
            {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "drainEncoder:encoder output video_encoder_format changed: " + newFormat);
            }
            else if (encoderStatus < 0)
            {
                Log.w(TAG, "drainEncoder:unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                // let's ignore it
            }
            else
            {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null)
                {
                    throw new RuntimeException("drainEncoder:encoderOutputBuffer " + encoderStatus + " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(TAG, "drainEncoder:ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0)
                {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & BUFFER_FLAG_END_OF_STREAM) != 0)
                {
                    Log.w(TAG, "drainEncoder:reached end of stream unexpectedly");
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private static void releaseEncoder()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG, "releaseEncoder:start ...");
            if (mEncoder != null)
            {
                try
                {
                    drainEncoder();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mEncoder.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mEncoder.release();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                mEncoder = null;
                Log.d(TAG, "releaseEncoder:SUCCESS");
            }
            else
            {
                Log.d(TAG, "releaseEncoder:already released");
            }
        }
    }

    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------
    // ------------ H264 EN-coder ------------


    // ------------ H264 Decoder ------------
    // ------------ H264 Decoder ------------
    // ------------ H264 Decoder ------------

    public static void feed_h264_decoder(final byte[] buf, final long buf_size, final int offset)
    {
        if ((buf != null) && (buf_size > 0))
        {
            if (buf_size > 30)
            {
                Log.d(TAG,
                      "feed_h264_decoder:buf=: " + buf[offset + 0] + " " + buf[offset + 1] + " " + buf[offset + 2] +
                      " " + buf[offset + 3] + " " + buf[offset + 4] + " " + buf[offset + 5] + " H " +
                      buf[offset + (int) buf_size - 2] + " " + buf[offset + (int) buf_size - 1]);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            {
                /* Find an unallocated input buffer to store food */
                try
                {
                    int inputBufferIndex = mDecoder_h264.dequeueInputBuffer(
                            0); // This method will return immediately if timeoutUs == 0
                    if (inputBufferIndex >= 0)
                    {
                        /* Get input buffer and fill it with our input */
                        ByteBuffer inputBuffer = mDecoder_h264.getInputBuffer(inputBufferIndex);
                        inputBuffer.clear();
                        inputBuffer.put(buf);
                        /* Enqueue buffer */
                        Log.d(TAG, "feed_h264_decoder:Enqueued input index: " + inputBufferIndex);

                        long ptsUsec = computePresentationTime(); // TODO: make good
                        mDecoder_h264.queueInputBuffer(inputBufferIndex, offset, (int) buf_size, ptsUsec, 0);
                    }
                }
                catch (Exception e)
                {
                    Log.d(TAG, "feed_h264_decoder:Get free buffer failed");
                }
            }
            else
            {
                // TODO: do something here?
            }
        }
    }

    static class h264_decoder_output_data
    {
        byte[] data;
        int frame_width;
        int frame_height;
    }

    public static h264_decoder_output_data fetch_from_h264_decoder()
    {
        h264_decoder_output_data ret = new h264_decoder_output_data();
        ret.data = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int decoderStatus = MediaCodec.INFO_TRY_AGAIN_LATER;

            try
            {
                if (mDecoder_h264 == null)
                {
                    return ret;
                }
                decoderStatus = mDecoder_h264.dequeueOutputBuffer(info,
                                                                  10000); // Dequeue an output buffer, block at most "timeoutUs" microseconds.
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.d(TAG, "fetch_from_h264_decoder:dequeueOutputBuffer:EE:" + e.getMessage());
                return ret;
            }

            if (info.flags == BUFFER_FLAG_END_OF_STREAM)
            {
                Log.d(TAG, "fetch_from_h264_decoder:flags: BUFFER_FLAG_END_OF_STREAM");
            }
            else
            {
                Log.d(TAG, "fetch_from_h264_decoder:flags: " + info.flags);
            }

            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                // no output available yet
                Log.d(TAG, "fetch_from_h264_decoder:no output from decoder available");
                return ret;
            }
            else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // not expected for an decoder
                Log.d(TAG, "fetch_from_h264_decoder:old_format: " + video_decoder_h264_format);
                MediaFormat newFormat = mDecoder_h264.getOutputFormat();
                Log.d(TAG, "fetch_from_h264_decoder:new_format: " + newFormat);
                ByteBuffer csd0 = newFormat.getByteBuffer("csd-0");
                ByteBuffer csd1 = newFormat.getByteBuffer("csd-1");
                System.out.println("fetch_from_h264_decoder:csd-0:len=" + csd0.limit());
                System.out.println("fetch_from_h264_decoder:csd-1:len=" + csd1.limit());

                video_decoder_h264_format = newFormat;

                return ret;
            }
            else if (decoderStatus < 0)
            {
                // Log.d(TAG,
                //      "fetch_from_h264_decoder:unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    Log.d(TAG, "fetch_from_h264_decoder:getMetrics=" + mDecoder_h264.getMetrics());
                }

                return ret;
            }
            else // encoderStatus >= 0
            {
                /* Compressed frame is ready! */
                ByteBuffer compressed = mDecoder_h264.getOutputBuffer(decoderStatus);
                if (compressed == null)
                {
                    Log.d(TAG, "fetch_from_h264_decoder:decoderOutputBuffer " + decoderStatus + " was null");
                    return ret;
                }
                else
                {
                    Log.d(TAG, "fetch_from_h264_decoder:Dequeue output index: " + decoderStatus);
                    // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                    compressed.position(info.offset);
                    compressed.limit(info.offset + info.size);
                    /* Copy to byte array for further processing */
                    byte[] arr = new byte[compressed.remaining()];
                    compressed.get(arr);
                    compressed.position(info.offset);

                    /* Release MediaCodec buffer */
                    mDecoder_h264.releaseOutputBuffer(decoderStatus, false);
                    ret.data = arr;
                    return ret;
                }
            }
        }
        else
        {
            // TODO: do something here?
        }

        return ret;
    }

    /**
     * Configures the H264 decoder
     */
    static void prepareDecoder()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            mBufferInfo_h264_decoder = new MediaCodec.BufferInfo();
            video_decoder_h264_format = MediaFormat.createVideoFormat(MIME_TYPE, 480, 640);

            // byte[] header_sps = {0x00, 0x00, 0x00, 0x01, 0x67, 0x42, (byte) 0x80, 0x0C, (byte) 0xE4, 0x40, (byte) 0xA0, (byte) 0xFD, 0x00, (byte) 0xDA, 0x14, 0x26, (byte) 0xA0};
            // byte[] header_pps = {0x00, 0x00, 0x00, 0x01, 0x68, (byte) 0xCE, 0x38, (byte) 0x80};
            // video_decoder_h264_format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            // video_decoder_h264_format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            // video_decoder_h264_format.setInteger(MediaFormat.KEY_BIT_RATE, 10 * 1000);
            // video_decoder_h264_format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            Log.d(TAG, "prepareDecoder:video_decoder_format: " + video_decoder_h264_format);

            try
            {
                mDecoder_h264 = MediaCodec.createDecoderByType(MIME_TYPE);
                Log.d(TAG, "prepareDecoder:SUCCESS: " + mDecoder_h264.getCodecInfo());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG, "prepareDecoder:EE1: " + e.getMessage());
            }
            mDecoder_h264.configure(video_decoder_h264_format, null, null, 0);
            mDecoder_h264.start();
        }
    }

    /**
     * Extracts all pending data from the decoder.
     */
    private static void drainDecoder_h264()
    {
        final int TIMEOUT_USEC = 10000;
        Log.d(TAG, "drainDecoder_h264:start");

        ByteBuffer[] decoderOutputBuffers = mDecoder_h264.getOutputBuffers();
        while (true)
        {
            int decoderStatus = mDecoder_h264.dequeueOutputBuffer(mBufferInfo_h264_decoder, TIMEOUT_USEC);
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER)
            {
                break;      // out of while
            }
            else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
            {
                // not expected for an encoder
                decoderOutputBuffers = mDecoder_h264.getOutputBuffers();
            }
            else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
            {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mDecoder_h264.getOutputFormat();
                Log.d(TAG, "drainEncoder:decoder output video_encoder_format changed: " + newFormat);
            }
            else if (decoderStatus < 0)
            {
                Log.w(TAG, "drainEncoder:unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
                // let's ignore it
            }
            else
            {
                ByteBuffer decodedData = decoderOutputBuffers[decoderStatus];
                if (decodedData == null)
                {
                    throw new RuntimeException("drainEncoder:decoderOutputBuffer " + decoderStatus + " was null");
                }

                if ((mBufferInfo_h264_decoder.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0)
                {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(TAG, "drainEncoder:ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo_h264_decoder.size = 0;
                }

                mDecoder_h264.releaseOutputBuffer(decoderStatus, false);

                if ((mBufferInfo_h264_decoder.flags & BUFFER_FLAG_END_OF_STREAM) != 0)
                {
                    Log.w(TAG, "drainEncoder:reached end of stream unexpectedly");
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Releases decoder resources.  May be called after partial / failed initialization.
     */
    private static void releaseDecoder_h264()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG, "releaseDecoder_h264:start ...");
            if (mDecoder_h264 != null)
            {
                try
                {
                    drainDecoder_h264();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mDecoder_h264.stop();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    mDecoder_h264.release();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                mDecoder_h264 = null;
                Log.d(TAG, "releaseDecoder_h264:SUCCESS");
            }
            else
            {
                Log.d(TAG, "releaseDecoder_h264:already released");
            }
        }
    }

    // ------------ H264 Decoder ------------
    // ------------ H264 Decoder ------------
    // ------------ H264 Decoder ------------

    static void stop_ringtone()
    {
        try
        {
            mMediaPlayer.stop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            mMediaPlayer.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void start_ringtone()
    {
        try
        {
            Uri ringtone_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getApplicationContext(), ringtone_uri);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0)
            {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(true);
                try
                {
                    mMediaPlayer.prepare();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                mMediaPlayer.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
