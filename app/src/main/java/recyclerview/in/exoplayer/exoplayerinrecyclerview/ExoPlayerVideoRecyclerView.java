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
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;

import java.util.ArrayList;
import java.util.List;


public class ExoPlayerVideoRecyclerView extends RecyclerView {

    private List<VideoInfo> videoInfoList = new ArrayList<>();
    private int videoSurfacedefaultHeight = 0;
    private int screendefaultHeight = 0;

    //surface view for playing video
    private CustomVideoSurfaceView videoSurfaceView;


    private Context appContext;


    /**
     * the position of playing video
     */
    private int playPosition = -1;

    private boolean addedVideo = false;
    private View rowParent;

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
     */
    //remove the player from the row
    private void removeVideoView(CustomVideoSurfaceView videoView) {

        ViewGroup parent = (ViewGroup) videoView.getParent();

        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            addedVideo = false;
        }
    }

    private void checkVideoSurfaceOutOfScreen() {

    }

    //play the video in the row
    public void playVideo() {

        int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

        if (startPosition < 0 || endPosition < 0) {
            return;
        }

        int targetPosition;
        if (startPosition != endPosition) {
            int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
            int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
            targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
        } else {
            targetPosition = startPosition;
        }

        if (targetPosition < 0 || targetPosition == playPosition) {
            return;
        }
        playPosition = targetPosition;
        videoSurfaceView.setVisibility(INVISIBLE);
        videoSurfaceView.stopPlayer();
        removeVideoView(videoSurfaceView);

        // get target View targetPosition in RecyclerView
        int at = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return;
        }

        ExoPlayerVideoRecyclerViewAdapter.VideoViewHolder holder
                = (ExoPlayerVideoRecyclerViewAdapter.VideoViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }

        holder.videoContainer.addView(videoSurfaceView);
        addedVideo = true;
        rowParent = holder.parent;

        videoSurfaceView.startPlayer(Uri.parse(videoInfoList.get(targetPosition).videoUrl));
    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location01 = new int[2];
        child.getLocationInWindow(location01);

        if (location01[1] < 0) {
            return location01[1] + videoSurfacedefaultHeight;
        } else {
            return screendefaultHeight - location01[1];
        }
    }


    private void initialize(Context context) {

        appContext = context.getApplicationContext();

        // 画面の中央位置を取得する。
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        videoSurfacedefaultHeight = point.x;
        screendefaultHeight = point.y;

        videoSurfaceView = new CustomVideoSurfaceView(appContext);

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    playVideo();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (addedVideo && rowParent != null && rowParent.equals(view)) {

                    removeVideoView(videoSurfaceView);
                    playPosition = -1;

                    videoSurfaceView.setVisibility(INVISIBLE);
                }

            }
        });
    }

    /**
     * release memory
     */
    public void onRelease() {

        if (videoSurfaceView != null) {
            videoSurfaceView.onRelease();
            videoSurfaceView = null;
        }

        rowParent = null;
    }


}
