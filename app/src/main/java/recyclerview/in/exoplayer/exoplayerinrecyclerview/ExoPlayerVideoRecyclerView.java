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
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


public class ExoPlayerVideoRecyclerView extends RecyclerView {

    private List<VideoInfo> videoInfoList = new ArrayList<>();

    //surface view for playing video
    private CustomVideoSurfaceView videoSurfaceView;


    private FrameLayout videoFrame;

    private Context appContext;


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
        }

    }

    //play the video in the row
    private void play(int position) {
        if (position == playPosition) {
            return;
        }

        playPosition = position;
        videoSurfaceView.setVisibility(INVISIBLE);
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
            playPosition = -1;
            return;
        }

        holder.videoContainer.addView(videoSurfaceView);
        videoFrame = holder.videoContainer;

        videoSurfaceView.preparePlayer(Uri.parse(videoInfoList.get(position).videoUrl));
    }


    private void initialize(Context context) {

        appContext = context.getApplicationContext();

        videoSurfaceView = new CustomVideoSurfaceView(appContext);

//        videoSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
//                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                        getResources().getDimension(R.dimen.exoplayer_video_height)
//                        , getResources().getDisplayMetrics())));


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


    /**
     * release memory
     */
    public void onRelease() {

        videoSurfaceView = null;
    }


}
