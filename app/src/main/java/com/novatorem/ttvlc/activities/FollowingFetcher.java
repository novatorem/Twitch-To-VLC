package com.novatorem.ttvlc.activities;

import com.novatorem.ttvlc.model.ChannelInfo;

import java.util.List;

public interface FollowingFetcher {
    void addStreamer(ChannelInfo streamer);
    void addStreamers(List<ChannelInfo> streamers);
    void showErrorView();
    boolean isEmpty();
    void notifyFinishedAdding();
}
