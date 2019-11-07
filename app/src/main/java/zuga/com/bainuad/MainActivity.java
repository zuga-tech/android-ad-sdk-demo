package zuga.com.bainuad;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.studymongolian.mongollibrary.MongolTextView;

import java.util.ArrayList;
import java.util.List;

import zuga.com.bainu.ad.sdk.AdLoader;
import zuga.com.bainu.ad.sdk.bean.UnifiedNativeAd;
import zuga.com.bainu.ad.sdk.bean.User;
import zuga.com.bainu.ad.sdk.listener.AdListener;
import zuga.com.bainu.ad.sdk.listener.NativeAdListener;
import zuga.com.bainu.ad.sdk.view.NativeAdView;

/**
 * @author saqrag
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Timeline> timelines;
    private final static String TAG = "MainActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        AdLoader.init(getApplication(),
                "8d09779b-a804-470e-9f54-d5ad7f4941d0",
                "ccd4f081-1e53-4079-8cff-a9eae9f4692f",
                new User.Builder()
                        .setGender(0)
                        .build());

        AdLoader.forLauncherAd(this)
                .setSilenceTime(10000)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                    }

                    @Override
                    public void onAdClosed() {
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                    }

                    @Override
                    public void onAdShow() {
                    }
                }).build().load();
        initData();
    }

    private void initData() {
        timelines = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            if (i % 5 == 0) {
                String adPositionId = "nativeAdId:" + i;
                timelines.add(new Timeline(Timeline.TYPE_AD, adPositionId, null));
                requestAd(adPositionId);
            } else {
                timelines.add(new Timeline(Timeline.TYPE_STRING, null, "   " + i));
            }
        }
        adapter = new RecyclerView.Adapter() {
            private final int viewTypeString = 0;
            private final int viewTypeAd = 1;

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    case viewTypeAd:
                        NativeAdView nativeAdView = (NativeAdView) LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_native_ad, parent, false);
                        nativeAdView.setIconView(nativeAdView.findViewById(R.id.iv_header))
                                .setHeaderLineView(nativeAdView.findViewById(R.id.tv_header))
                                .setAdvertiserView(null)
                                .setBodyView(nativeAdView.findViewById(R.id.tv_body))
                                .setMediaView(nativeAdView.findViewById(R.id.mv_media));
                        nativeAdView.getMediaView().setImageView(nativeAdView.findViewById(R.id.iv_media));
                        nativeAdView.getMediaView().setVideoView(nativeAdView.findViewById(R.id.vv_media));
                        nativeAdView.getLayoutParams().width = 0;
                        return new RecyclerView.ViewHolder(nativeAdView) {
                        };
                    case viewTypeString:
                    default:
                        MongolTextView textView = new MongolTextView(parent.getContext());
                        textView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.MATCH_PARENT));
                        return new RecyclerView.ViewHolder(textView) {
                        };
                }
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Timeline timeline = timelines.get(position);
                switch (timeline.type) {
                    case Timeline.TYPE_STRING:
                        ((MongolTextView) holder.itemView).setText(timeline.getContent(String.class));
                        break;
                    case Timeline.TYPE_AD:
                        UnifiedNativeAd nativeAd = timeline.getContent(UnifiedNativeAd.class);
                        NativeAdView nativeAdView = (NativeAdView) holder.itemView;
                        if (nativeAd == null) {
                            nativeAdView.getLayoutParams().width = 0;
                            nativeAdView.setVisibility(View.GONE);
                            nativeAdView.requestLayout();
                        } else {
                            Glide.with(nativeAdView).load(nativeAd.getIcon()).into((ImageView) nativeAdView.getIconView());
                            ((MongolTextView) nativeAdView.getHeaderLineView()).setText(nativeAd.getHeadline());
                            //noinspection ResultOfMethodCallIgnored
                            nativeAdView.getAdvertiserView();
                            ((MongolTextView) nativeAdView.getBodyView()).setText(nativeAd.getBody());
                            nativeAdView.getLayoutParams().width = RecyclerView.LayoutParams.WRAP_CONTENT;
                            if (nativeAd.getMediaType() == UnifiedNativeAd.MEDIA_TYPE_IMAGE) {
                                Glide.with(nativeAdView).load(nativeAd.getMedia()).into((ImageView) nativeAdView.getMediaView().getImageView());
                                nativeAdView.getMediaView().getImageView().setVisibility(View.VISIBLE);
                                nativeAdView.getMediaView().getVideoView().setVisibility(View.GONE);
                            } else {
                                VideoView videoView = (VideoView) nativeAdView.getMediaView().getVideoView();
                                videoView.setVideoPath(nativeAd.getMedia());
                                videoView.start();
                                nativeAdView.getMediaView().getVideoView().setVisibility(View.VISIBLE);
                                nativeAdView.getMediaView().getImageView().setVisibility(View.GONE);
                            }
                            nativeAdView.setVisibility(View.VISIBLE);
                            nativeAdView.setNativeAd(nativeAd);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public int getItemCount() {
                return timelines.size();
            }

            @Override
            public int getItemViewType(int position) {
                Timeline timeline = timelines.get(position);
                if (timeline.type == Timeline.TYPE_STRING) {
                    return viewTypeString;
                } else {
                    return viewTypeAd;
                }
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void requestAd(String adPositionId) {
        AdLoader.forNativeAd(this)
                .setAdPositionId(adPositionId)
                .withNativeAdListener(new NativeAdListener() {
                    @Override
                    public void onNativeAdLoaded(UnifiedNativeAd nativeAd) {
                        for (Timeline timeline : timelines) {
                            if (adPositionId.equals(timeline.id)) {
                                timeline.content = nativeAd;
                                break;
                            }

                        }
                    }

                    @Override
                    public void onMediaViewClick(View mediaView) {
                        Log.d(TAG, "onMediaViewClick");
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdClicked() {
                        Log.d(TAG, "onAdClicked");
                    }

                    @Override
                    public void onAdClosed() {
                        Log.d(TAG, "onAdClosed");
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.d(TAG, "onAdFailedToLoad" + errorCode);
                    }

                    @Override
                    public void onAdShow() {
                        Log.d(TAG, "onAdShow");
                    }
                }).build().load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Timeline timeline : timelines) {
            if (timeline.type == Timeline.TYPE_AD) {
                UnifiedNativeAd content = timeline.getContent(UnifiedNativeAd.class);
                if (content != null) {
                    content.destroy();
                }
            }
        }
    }

    private class Timeline {
        public final static int TYPE_AD = 1;
        public final static int TYPE_STRING = 2;
        int type;
        String id;
        Object content;

        public Timeline(int type, String id, Object content) {
            this.type = type;
            this.id = id;
            this.content = content;
        }

        public <B> B getContent(Class<B> clazz) {
            boolean instance = clazz.isInstance(content);
            if (instance) {
                return clazz.cast(content);
            } else {
                return null;
            }
        }
    }
}
