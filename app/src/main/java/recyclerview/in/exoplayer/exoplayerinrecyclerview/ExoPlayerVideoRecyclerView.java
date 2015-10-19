/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;


public class ExoPlayerVideoRecyclerView extends RecyclerView
        implements AudioCapabilitiesReceiver.Listener, MediaCodecVideoTrackRenderer.EventListener,
        SurfaceHolder.Callback {

    private static final int DEFAULT_PLAY_POSITION = -1;
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;

    private List<VideoInfo> videoInfoList = new ArrayList<>();

    //surface view for playing video
    private SurfaceView videoSurfaceView;

    private boolean surfaceViewViable = false;

    //fields about video
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private ExoPlayer player;
    private static final CookieManager defaultCookieManager;
    private ExtractorSampleSource sampleSource;
    private Handler mainHandler;
    private Allocator allocator;
    private DataSource dataSource;
    private AspectRatioFrameLayout videoFrame;

    private Context appContext;

    private MediaCodecVideoTrackRenderer videoRenderer;
    private MediaCodecAudioTrackRenderer audioRenderer;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    /**
     * the position of playing video
     */
    private int playPosition;

    /**
     * {@inheritDoc}
     *
     * @param context
     */
    public ExoPlayerVideoRecyclerView(Context context) {
        super(context);
        initialize(context);
    }

    public void setVideoInfoList(List<VideoInfo> videoInfoList) {
        this.videoInfoList = videoInfoList;
    }

    /**
     * {@inheritDoc}
     *
     * @param context
     * @param attrs
     */
    public ExoPlayerVideoRecyclerView(Context context,
                                      AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * {@inheritDoc}
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public ExoPlayerVideoRecyclerView(Context context,
                                      AttributeSet attrs,
                                      int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /**
     * prepare for video play
     *
     * @param position
     */
    private void preparePlayer(int position) {

        Uri uri = Uri.parse(videoInfoList.get(position).videoUrl);

        // Build the sample source
        sampleSource =
                new ExtractorSampleSource(uri, dataSource, allocator, 10 * BUFFER_SEGMENT_SIZE);

        // Build the track renderers
        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, -1, mainHandler, this, -1);
        audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        // Build the ExoPlayer and start playback
        player.prepare(videoRenderer, audioRenderer);

        playVideo();
    }

    //method to realy do the play
    private void playVideo() {
        if (surfaceViewViable) {
            player.sendMessage(videoRenderer,
                    MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                    videoSurfaceView.getHolder().getSurface());
            player.setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void removeVideoView(SurfaceView videoView) {

        ViewGroup parent = (ViewGroup) videoView.getParent();

        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
        }

    }

    private void play(int position) {
        if (position == playPosition) {
            return;
        }

        playPosition = position;
        removeVideoView(videoSurfaceView);

        // get target View position in RecyclerView
        int at = position - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return;
        }

        ExoPlayerVideoRecyclerViewAdapter.VideoViewHolder holder
                = (ExoPlayerVideoRecyclerViewAdapter.VideoViewHolder) child.getTag();
        if (holder == null) {
            playPosition = DEFAULT_PLAY_POSITION;
            return;
        }
        holder.videoContainer.addView(videoSurfaceView);
        videoFrame = holder.videoContainer;

        preparePlayer(playPosition);
    }


    private void initialize(Context context) {
        mainHandler = new Handler();

        appContext = context.getApplicationContext();

        allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        dataSource =
                new DefaultUriDataSource(appContext,
                        new DefaultBandwidthMeter(mainHandler, null),
                        Util.getUserAgent(appContext, "ExoPlayerDemo"));


        videoSurfaceView = new SurfaceView(appContext);

        videoSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        getResources().getDimension(R.dimen.exoplayer_video_height)
                        , getResources().getDisplayMetrics())));

        videoSurfaceView.getHolder().addCallback(this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(appContext, this);
        audioCapabilitiesReceiver.register();

        player = ExoPlayer.Factory.newInstance(2);
        player.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        break;
                    case ExoPlayer.STATE_ENDED:
                        player.seekTo(0);
                        break;
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        break;
                    case ExoPlayer.STATE_READY:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPlayWhenReadyCommitted() {
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }
        });


        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    play(getPlayTargetPosition());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private int getPlayTargetPosition() {
        return ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
    }

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {

    }

    /**
     * release memory
     */
    public void onRelease() {
        audioCapabilitiesReceiver.unregister();

        releasePlayer();

        if (mainHandler != null) {
            mainHandler = null;
        }

        allocator = null;
        dataSource = null;
        videoRenderer = null;
        audioRenderer = null;
        sampleSource = null;

        videoSurfaceView = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceViewViable = true;
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceViewViable = false;
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if (videoFrame != null) {
            videoFrame.setAspectRatio(
                    height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
        }
    }

    @Override
    public void onDrawnToSurface(Surface surface) {

    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {

    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {

    }

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {

    }
}
