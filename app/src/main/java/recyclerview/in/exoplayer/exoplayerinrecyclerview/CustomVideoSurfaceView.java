package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.audio.AudioTrack;
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

/**
 * リストでの動画再生用にカスタマイズした特殊な{@link android.view.View View}。
 */
public class CustomVideoSurfaceView extends FrameLayout
        implements AudioCapabilitiesReceiver.Listener,
        MediaCodecVideoTrackRenderer.EventListener,
        MediaCodecAudioTrackRenderer.EventListener,
        SurfaceHolder.Callback,
        View.OnClickListener {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static CustomVideoSurfaceView instance;

    //fields about playing video
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private ExoPlayer player;
    private static final CookieManager defaultCookieManager;
    private ExtractorSampleSource sampleSource;
    private Handler mainHandler;
    private Allocator allocator;
    private DataSource dataSource;

    private Uri currentUri;
    private Context appContext;

    private int defaultWidth = 0;
    private int videoWidth, videoHeight, viewWidth, viewHeight;
    private float aspect;
    private FrameLayout.LayoutParams layoutParams;
    private SurfaceView videoSurfaceView;

    private MediaCodecVideoTrackRenderer videoRenderer;
    private MediaCodecAudioTrackRenderer audioRenderer;

    private boolean surfaceViewViable = false;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }


    public void startPlayer(Uri uri) {

        currentUri = uri;

        player.seekTo(0);

        // Build the sample source
        sampleSource =
                new ExtractorSampleSource(uri, dataSource, allocator, 10 * BUFFER_SEGMENT_SIZE);

        // Build the track renderers
        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, -1, mainHandler, this, -1);
        audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, mainHandler, this);

        // Build the ExoPlayer and start playback
        player.prepare(videoRenderer, audioRenderer);
        player.setPlayWhenReady(true);

        playVideo();
    }

    //method to actually do the play
    private void playVideo() {

        if (surfaceViewViable) {
            player.sendMessage(videoRenderer,
                    MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                    videoSurfaceView.getHolder().getSurface());
        }

    }

    //release the player
    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * release memory
     */
    public void onRelease() {
        releasePlayer();

        audioCapabilitiesReceiver.unregister();

        if (mainHandler != null) {
            mainHandler = null;
        }

        allocator = null;
        dataSource = null;
        videoRenderer = null;
        audioRenderer = null;
        sampleSource = null;

        instance = null;

    }

    public static CustomVideoSurfaceView getInstance(Context context) {
        if (instance != null) {
            return instance;
        } else {
            instance = new CustomVideoSurfaceView(context);
            return instance;
        }
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void onRestartPlayer() {
        makePlayer();
        if (currentUri != null) {
            startPlayer(currentUri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        player.setPlayWhenReady(!player.getPlayWhenReady());
    }


    /**
     * 初期化処理を行う。
     */
    protected void initialize(Context context) {
        appContext = context.getApplicationContext();

        setVisibility(INVISIBLE);

        // 画面の中央位置を取得する。
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        defaultWidth = point.x;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(R.layout.exoplayer_video_surface_view, null));


        videoSurfaceView = (SurfaceView) this.findViewById(R.id.video_surface_view);
        initializeVideoPlayer();
    }

    private void initializeVideoPlayer() {
        mainHandler = new Handler();


        allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        dataSource =
                new DefaultUriDataSource(appContext,
                        new DefaultBandwidthMeter(mainHandler, null),
                        Util.getUserAgent(appContext, "ExoPlayerDemo"));


        videoSurfaceView.getHolder().addCallback(this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(appContext, this);
        audioCapabilitiesReceiver.register();

        makePlayer();

    }

    private void makePlayer() {
        if (player != null) {
            return;
        }

        player = ExoPlayer.Factory.newInstance(2);
        player.addListener(new ExoPlayer.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                String text = "playWhenReady=" + playWhenReady + ", playbackState=";
                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        text += "buffering";
                        setVisibility(VISIBLE);
                        videoSurfaceView.setAlpha(0);
                        videoSurfaceView.setOnClickListener(null);


                        break;
                    case ExoPlayer.STATE_ENDED:
                        player.seekTo(0);
                        text += "ended";
                        break;
                    case ExoPlayer.STATE_IDLE:
                        text += "idle";
                        break;
                    case ExoPlayer.STATE_PREPARING:
                        text += "preparing";
                        break;
                    case ExoPlayer.STATE_READY:
                        videoSurfaceView.setOnClickListener(CustomVideoSurfaceView.this);
                        videoSurfaceView.setAlpha(1);
                        text += "ready";
                        break;
                    default:
                        text += "unknown";
                        break;
                }

                Log.d("20672067", text);

            }

            @Override
            public void onPlayWhenReadyCommitted() {
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.d("20672067", "somethingwrong:" + "onPlayerError:" + error.toString());

            }
        });


    }

    public void stopPlayer() {
        if (player != null) {
            player.stop();
        }
    }

    protected void calculateAspectRatio(int width, int height) {
        viewWidth = defaultWidth;
        viewHeight = defaultWidth;

        videoWidth = width;
        videoHeight = height;

        aspect = (float) videoWidth / videoHeight;

        layoutParams = (FrameLayout.LayoutParams) getLayoutParams();

        if (((float) viewWidth / videoWidth) > ((float) viewHeight / videoHeight)) {
            layoutParams.width = (int) (viewHeight * aspect + 0.5F);
            layoutParams.height = viewHeight;
        } else {
            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth / aspect + 0.5F);
        }

        layoutParams.gravity = Gravity.CENTER;


        Log.d("20672067", "calculateAspectRatio:" + layoutParams.width + "--" + layoutParams.height);

        setLayoutParams(layoutParams);
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
        calculateAspectRatio(width, height);
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

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
    }

    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
        Log.d("20672067", "somethingwrong:" + "onAudioTrackInitializationError:" + e.toString());
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
        Log.d("20672067", "somethingwrong:" + "onAudioTrackWriteError:" + e.toString());
    }

}
