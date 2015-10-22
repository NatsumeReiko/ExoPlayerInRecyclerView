package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/**
 * リストでの動画再生用にカスタマイズした特殊な{@link android.view.View View}。
 */
public class CustomVideoSurfaceView extends FrameLayout implements TextureView.SurfaceTextureListener, View.OnClickListener {


    /**
     * DiskCacheを使用するかどうか。
     */
    protected static final boolean USE_DISK_CACHE = true;
    private static final int PROGRESS_SIZE = 50;

    /**
     * DiskCacheの最大容量。
     * とりあえず1GB。
     */
    protected static final int DISK_CACHE_MAX_SIZE = 1 * 1024 * 1024 * 1024;

    /**
     * DiskCacheのディレクトリ名。
     */
    protected static final String DISK_CACHE_DIRECTORY = "video";

    /**
     * RENDER_STARTが呼ばれない端末のために強制的に準備完了を送るまでの待ち時間。
     */
    protected static final int DELAY_TIME = 500;

    /**
     * ロックオブジェクト
     */
//    protected static final Object lock = new Object();

    /**
     * 動画の再生を行うための{@link android.media.MediaPlayer MediaPlayer}。
     */
//    protected MediaPlayer player;

    /**
     * G
     * 再生している動画を描画するための{@link android.graphics.SurfaceTexture SurfaceTexture}。
     */
//    protected SurfaceTexture texture;

    /**
     * 再生している動画を描画するための{@link android.view.Surface Surface}。
     */
//    protected Surface surface;

    /**
     * 動画再生の準備が完了した時のイベントを受け取るリスナ。
     */
//    protected MediaPlayer.OnPreparedListener listener;

    /**
     * DiskCache。
     */
//    protected LruDiscCache cache;

    /**
     * 動画の初期横幅。
     */
    protected int defaultWidth = 0;
    int videoWidth, videoHeight, viewWidth, viewHeight;
    float aspect;
    ViewGroup.LayoutParams layoutParams;
    private ProgressBar progressA, progressB;
    private SurfaceView videoView;

    /**
     * {@link android.os.Handler Handler}。
     */
//    protected Handler handler;

    /**
     * レンダリングが始まった時実行される{@link java.lang.Runnable Runnable}。
     */
//    protected Runnable renderStart = null;

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    public CustomVideoSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    public CustomVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    public CustomVideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        setSurface(surface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        setSurface(surface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        setSurface(surface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
//        MediaPlayer player = this.player;
//        if (player.isPlaying()) {
//            player.pause();
//        } else {
//            player.start();
//        }
    }

    /**
     * 読み込む動画のURLを設定する。
     * 設定した段階で動画再生準備を非同期で始める。
     *
     * @param url 再生する動画のURL。
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView setVideoURL(final String url) {
//        // Disk操作や動画操作を行うためメインスレッドを阻害しないように別スレッドで行う。
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    final MediaPlayer player = CustomVideoTextureView.this.player;
//
//                    synchronized (lock) {
//
//                        // MediaPlayerが破棄済みだったら終了する。
//                        if (player == null) {
//                            return null;
//                        }
//
//                        // {@link android.media.MediaPlayer MediaPlayer}を初期化する。
//                        initializePlayer();
//
//                        // DiskCacheを使用しない場合。
//                        if (!USE_DISK_CACHE) {
//
//                            // 再生する動画のURLを設定する。
//                            player.setDataSource(url);
//
//                            // 非同期で動画再生の準備を行う。
//                            player.prepareAsync();
//
//                            return null;
//                        }
//
//                        LruDiscCache cache = CustomVideoTextureView.this.cache;
//
//                        // DiskCacheが使えない時はそのままURLから動画を再生する。
//                        if (cache == null) {
//
//                            // 再生する動画のURLを設定する。
//                            player.setDataSource(url);
//
//                            // 非同期で動画再生の準備を行う。
//                            player.prepareAsync();
//
//                            return null;
//                        }
//
//                        File file = null;
//
//                        // DiskCacheから取得できるか試みる。
//                        try {
//                            file = getCache(url, cache);
//                        } catch (IllegalArgumentException e) {
//                            // 別スレッド処理の関係でエラーが出てキャッシュ再生できなかったとしてもURLから再生できるので問題なし。
//                            e.printStackTrace();
//                        }
//
//                        // DiskCacheが存在する場合はキャッシュファイルから再生。
//                        if (file != null) {
//
//                            // 再生する動画のファイルパスを設定する。
//                            FileInputStream stream = new FileInputStream(file);
//                            player.setDataSource(stream.getFD());
//                            stream.close();
//                        } else {
//
//                            // 再生する動画のURLを設定する。
//                            player.setDataSource(url);
//
//                            // 再生する動画のキャッシュを生成する。
//                            cacheVideo(url);
//                        }
//
//                        // 非同期で動画再生の準備を行う。
//                        try {
//                            player.prepareAsync();
//                        } catch (IllegalStateException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                } catch (IOException e) {
//                    // 動画が読み込めなかった場合には何もしようがないので無視。
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute();
//
//        return this;
//    }

    /**
     * 使わなくなったリソースなどを開放する。
     *
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView destroy() {
//
//        synchronized (lock) {
//            // 動画の再生を行うためのMediaPlayerを解放する。
//            MediaPlayer player = this.player;
//            if (player != null) {
//                player.release();
//                this.player = null;
//            }
//
//            // 再生している動画を描画するためのSurfaceTextureを解放する。
//            SurfaceTexture texture = this.texture;
//            if (texture != null) {
//                texture.release();
//                this.texture = null;
//            }
//
//            // 再生している動画を描画するためのSurfaceを解放する。
//            Surface surface = this.surface;
//            if (surface != null) {
//                surface.release();
//                this.surface = null;
//            }
//
//            // DiskCacheを解放する。
//            LruDiscCache cache = this.cache;
//            if (cache != null) {
//                cache.close();
//                this.cache = null;
//            }
//
//            listener = null;
//        }
//        return this;
//    }

    /**
     * 動画を再生する。
     *
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView play() {
//        synchronized (lock) {
//            MediaPlayer player = this.player;
//
//            // MediaPlayerが破棄済みだったら終了する。
//            if (player == null) {
//                return null;
//            }
//
//            player.start();
//        }
//        return this;
//    }

    /**
     * 動画を停止する。
     *
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView stop() {
//        synchronized (lock) {
//            MediaPlayer player = this.player;
//
//            // MediaPlayerが破棄済みだったら終了する。
//            if (player == null) {
//                return null;
//            }
//
//            // 再生中の時のみ停止メソッドを実行する。
//            if (player.isPlaying()) {
//                player.stop();
//            }
//        }
//        return this;
//    }

    /**
     * 動画を一時停止する。
     *
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView pause() {
//        synchronized (lock) {
//            MediaPlayer player = this.player;
//
//            // MediaPlayerが破棄済みだったら終了する。
//            if (player == null) {
//                return null;
//            }
//
//            // 再生中の時のみ一時停止メソッドを実行する。
//            if (player.isPlaying()) {
//                player.pause();
//            }
//        }
//        return this;
//    }

    /**
     * {@link android.media.MediaPlayer MediaPlayer}をリセットする。
     *
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView reset() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                synchronized (lock) {
//                    MediaPlayer player = CustomVideoTextureView.this.player;
//
//                    // MediaPlayerが破棄済みだったら終了する。
//                    if (player == null) {
//                        return null;
//                    }
//
//                    resetPlayer();
//                }
//                return null;
//            }
//        }.execute();
//        return this;
//    }

    /**
     * 動画再生の準備が完了した時に呼び出すリスナを設定する。
     *
     * @param listener 動画再生の準備が完了した時に呼び出すリスナ。
     * @return メソッドが呼び出されたインスタンスの参照。
     */
//    public CustomVideoTextureView setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
//        this.listener = listener;
//        return this;
//    }

    /**
     * 初期化処理を行う。
     */
    protected void initialize(Context context) {
//        setSurfaceTextureListener(this);
//        player = new MediaPlayer();

        // 画面の中央位置を取得する。
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        defaultWidth = point.x;


        final ProgressBar progressA = new ProgressBar(context);
        final ProgressBar progressB = new ProgressBar(context);

        // ローディングのViewの属性を設定する。
        int size = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PROGRESS_SIZE, getResources().getDisplayMetrics()) + 0.5F);
        FrameLayout.LayoutParams progressLayoutParams = new FrameLayout.LayoutParams(size, size);
        progressLayoutParams.gravity = Gravity.CENTER;
        progressA.setLayoutParams(progressLayoutParams);
        progressA.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress));
        progressB.setLayoutParams(progressLayoutParams);
        progressB.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress_r));

        addView(progressA);
        addView(progressB);
        this.progressA = progressA;
        this.progressB = progressB;

        final SurfaceView videoView = new SurfaceView(context);

        FrameLayout.LayoutParams videoLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoLayoutParams.gravity = Gravity.CENTER;
        videoView.setLayoutParams(videoLayoutParams);

        addView(videoView);
//        videoView.setAlpha(0);
        this.videoView = videoView;


//        createDiskCache();
//        setOnClickListener(this);
//
//        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 描画用のSurfaceを設定する。
     *
     * @param texture 動画用の{@link android.graphics.SurfaceTexture SurfaceTexture}。
     */
//    protected void setSurface(final SurfaceTexture texture) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                synchronized (lock) {
//                    MediaPlayer player = CustomVideoTextureView.this.player;
//
//                    // MediaPlayerが破棄済みだったら終了する。
//                    if (player == null) {
//                        return null;
//                    }
//
//                    Surface surface = new Surface(texture);
//                    CustomVideoTextureView.this.player.setSurface(surface);
//                    CustomVideoTextureView.this.surface = surface;
//                    CustomVideoTextureView.this.texture = texture;
//                }
//                return null;
//            }
//        }.execute();
//    }

    /**
     * {@link android.media.MediaPlayer MediaPlayer}の初期化を行う。
     */
//    protected void initializePlayer() {
//        final MediaPlayer player = this.player;
//
//        // {@link android.media.MediaPlayer MediaPlayer}のステータスをリセットする。
//        resetPlayer();
//
//        // 描画用の{@link android.view.Surface Surface}を設定する。
//        player.setSurface(surface);
//
//        // 音再生用のストリームの種類を設定する。
//        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//        renderStart = new Runnable() {
//            @Override
//            public void run() {
//                // 動画再生の準備が完了した時のイベントを受け取るリスナがあれば設定する。
//                MediaPlayer.OnPreparedListener listener = CustomVideoTextureView.this.listener;
//                if (listener != null) {
//                    listener.onPrepared(player);
//                }
//                if (PreferenceManager.getAudioMute(getContext())) {
//                    if (player.isPlaying()) {
//                        player.setVolume(0.0F, 0.0F);
//                    }
//                }
//                CustomVideoTextureView.this.renderStart = null;
//            }
//        };
//
//        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                calculateAspectRatio();
//                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        if (player != null) {
//                            player.start();
//                        }
//                    }
//                });
//                player.start();
//                Runnable renderStart = CustomVideoTextureView.this.renderStart;
//                if (renderStart != null) {
//                    handler.postDelayed(renderStart, DELAY_TIME);
//                }
//            }
//        });
//
//        player.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
//                    Runnable renderStart = CustomVideoTextureView.this.renderStart;
//                    if (renderStart != null) {
//                        renderStart.run();
//                        handler.removeCallbacks(renderStart);
//                    }
//                }
//                return false;
//            }
//        });
//    }

    /**
     * {@link android.media.MediaPlayer MediaPlayer}のステータスをリセットする。
     */
//    protected void resetPlayer() {
//        MediaPlayer player = this.player;
//
//        // 再生されている場合は停止する。
//        stop();
//
//        player.setOnCompletionListener(null);
//
//        // すでに紐付いている描画用の{@link android.view.Surface Surface}を解除する。
//        player.setSurface(null);
//
//        //{@link android.media.MediaPlayer MediaPlayer}の内部状況を全てリセットする。
//        player.reset();
//    }
    protected void calculateAspectRatio(int width, int height) {
        viewWidth = defaultWidth;
        viewHeight = viewWidth;

        videoWidth = width;
        videoHeight = height;

        aspect = (float) videoWidth / videoHeight;

        layoutParams = getLayoutParams();

        if (((float) viewWidth / videoWidth) > ((float) viewHeight / videoHeight)) {
            layoutParams.width = (int) (viewHeight * aspect + 0.5F);
            layoutParams.height = viewHeight;
        } else {
            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth / aspect + 0.5F);
        }


        setLayoutParams(layoutParams);
    }

    public Surface getSurface() {
        return videoView.getHolder().getSurface();
    }

    public SurfaceHolder getSurfaceHold() {
        return videoView.getHolder();
    }

    /**
     * URIからDiskCacheのkeyを取得する。
     *
     * @param uri URI。
     * @return DiskCacheのkey。
     */
//    protected String getKey(String uri) {
//        String name = new File(uri).getParent();
//        int index = name.lastIndexOf(File.separator);
//        if (index >= 0) {
//            name = name.substring(index + 1);
//        }
////        new File(uri).getParent();
////        // URLのファイル名部分をそのまま保存する際のファイル名にする。
////        String name = uri.substring(uri.lastIndexOf(File.separator) + 1);
////        uri.lastIndexOf()
////        int index = name.indexOf(".");
////        if (index >= 0) {
////            name = name.substring(0, index);
////        }
//        return name;
//    }

    /**
     * DiskCacheを生成する。
     */
//    protected void createDiskCache() {
//        try {
//            Context context = getContext().getApplicationContext();
//
//            // 外部のSDなどは遅いので一旦候補から外す。
////            String path = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
//            // キャッシュを保存するパスを取得する。
//            String path = context.getCacheDir().getPath();
//            File directory = new File(path + File.separator + DISK_CACHE_DIRECTORY);
//
//            // ディレクトリがなければディレクトリを作成する。
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//
//            // DiskCacheを作成する。
//            cache = new LruDiscCache(directory, new FileNameGenerator() {
//                @Override
//                public String generate(String uri) {
//                    return getKey(uri);
//                }
//            }, DISK_CACHE_MAX_SIZE);
//        } catch (IOException e) {
//            e.printStackTrace();
//            //DiskCacheの作成に失敗したとしてもURLから再生でいるので問題なし。
//        }
//    }

    /**
     * 動画をDiskCacheに保存する。
     *
     * @param url DiskCacheに保存する動画のURL。
     */
//    protected void cacheVideo(final String url) {
//        new Thread() {
//            @Override
//            public void run() {
//                LruDiscCache cache = CustomVideoTextureView.this.cache;
//
//                // DiskCacheが破棄済みだったら終了する。
//                if (cache == null) {
//                    return;
//                }
//
//                try {
//
//                    // URLのストリームを開く。
//                    InputStream stream = new URL(url).openStream();
//
//                    // 開けなかった場合は何もしない。
//                    if (stream == null) {
//                        return;
//                    }
//
//                    // DiskCacheに保存する。
//                    try {
//                        cache.save(url, stream, null);
//                    } catch (IllegalStateException e) {
//                        // DiskCacheが生成できなかったとしてもURLから動画再生出来るので無視。
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        // DiskCacheが生成できなかったとしてもURLから動画再生出来るので無視。
//                        e.printStackTrace();
//                    } finally {
//                        stream.close();
//                    }
//                } catch (IOException e) {
//                    // DiskCacheが生成できなかったとしてもURLから動画再生出来るので無視。
//                    e.printStackTrace();
//                } catch (IllegalArgumentException e) {
//                    // DiskCacheが生成できなかったとしてもURLから動画再生出来るので無視。
//                    e.printStackTrace();
//                } catch (ArrayIndexOutOfBoundsException e) {
//                    // SERVERS-6169対応。
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }

    /**
     * キャッシュ―ファイルを取得
     *
     * @param url
     * @param cache
     * @return
     */
//    protected File getCache(String url, LruDiscCache cache) {
//        if (cache != null) {
//            return cache.get(url);
//        }
//        return null;
//    }
}
