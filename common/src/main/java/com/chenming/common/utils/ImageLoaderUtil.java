package com.chenming.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class ImageLoaderUtil {

    private static class ImageUtilHolder {
        static ImageLoaderUtil instance = new ImageLoaderUtil();
    }

    public static ImageLoaderUtil getInstance() {
        return ImageUtilHolder.instance;
    }

    /**
     * 加载普通图
     */
    public void loadNormalImg(ImageView imageView, String url, Drawable placeholderDrawable,
                              Drawable errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .centerCrop())
                .into(imageView);
    }

    /**
     * 加载普通图
     */
    public void loadNormalImgNoScaleType(ImageView imageView, String url, Drawable placeholderDrawable,
                                         Drawable errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable))
                .into(imageView);
    }

    /**
     * 加载资源gif图
     *
     * @param context
     * @param imageView
     * @param res_id
     */
    public void loadGifPhoto(Context context, ImageView imageView, int res_id, int default_id) {
        Glide.with(context)
                .asGif()
                .load(res_id)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .error(default_id))
                .into(imageView);

    }


    /**
     * 加载普通图
     */
    public void loadNormalImg(ImageView imageView, String url, int placeholderDrawable,
                              int errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .centerCrop())
                .into(imageView);
    }

    /**
     * 加载普通图
     */
    public void loadNormalImg(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .centerCrop())
                .into(imageView);
    }

    /**
     * 加载普通图
     */
    public void loadFitCenterImg(ImageView imageView, String url, int placeholderDrawable,
                                 int errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .fitCenter())
                .into(imageView);
    }

    /**
     * 加载普通图
     */
    public void loadFitCenterImg(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .fitCenter())
                .into(imageView);
    }

    /**
     * 加载普通图
     */
    public void loadImg(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .into(imageView);
    }


    /**
     * 加载普通图
     */
    public void loadCenterInsideImg(ImageView imageView, String url, int placeholderDrawable,
                                    int errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .centerInside())
                .into(imageView);
    }

    public void loadImg(ImageView imageView, int drawableId, int placeholderDrawable) {
        Glide.with(imageView.getContext())
                .load(drawableId)
                .apply(new RequestOptions().placeholder(placeholderDrawable))
                .into(imageView);
    }

    public void loadImg(ImageView imageView, int drawableId) {
        Glide.with(imageView.getContext())
                .load(drawableId)
                .apply(new RequestOptions())
                .into(imageView);
    }


    public void loadNormalNotCenterImg(ImageView imageView, String url, Drawable placeholderDrawable,
                                       Drawable errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable))
                .into(imageView);
    }

    public void loadNormalNotCenterImg(ImageView imageView, String url, int placeholderDrawable,
                                       int errorDrawable) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable))
                .into(imageView);
    }

    /**
     * 加载圆形图   有点小问题的
     */
    public void loadCircleImg(ImageView imageView, Object url, Drawable placeholderDrawable,
                              Drawable errorDrawable) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .centerCrop()
                )
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory
                                .create(CommApplication.Companion.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        loadCircleTransform(imageView.getContext(), errorDrawable, imageView);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        if (imageView.getDrawable() == null) {
                            loadCircleTransform(imageView.getContext(), placeholder, imageView);
                        }
                    }
                });
    }


    /**
     * 加载圆形图
     */
    public void loadCircleImg(ImageView imageView, String url, int placeholderDrawable,
                              int errorDrawable) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(RequestOptions
                        .bitmapTransform(new CircleCrop())
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable))
                .into(imageView);
    }

    /**
     * 加载圆形图
     */
    public void loadCircleImg(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(RequestOptions
                        .bitmapTransform(new CircleCrop()))
                .into(imageView);
    }

    private void loadCircleTransform(Context context, Drawable drawable, ImageView imageView) {
        Glide.with(context)
                .asDrawable()
                .load(drawable)
                .apply(new RequestOptions()
                        .transform(new GlideCircleTransform())
                        .centerCrop()
                        .circleCrop())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }
                });

    }

    /**
     * 加载本地圆角图
     */
    public void loadRoundFileImg(ImageView imageView, String path, int radius) {

        Glide.with(imageView.getContext())
                .load(path)
                .apply(new RequestOptions()
                        .centerCrop()
                        .transform(new GlideRoundTransform(radius)))
                .into(imageView);
    }

    /**
     * 加载本地图
     */
    public void loadFileImg(ImageView imageView, String path) {

        Glide.with(imageView.getContext())
                .load(path)
                .into(imageView);
    }

    /**
     * 加载圆角图
     */
    public void loadRoundImg(ImageView imageView, String url, Drawable placeholderDrawable,
                             Drawable errorDrawable, int radius) {

        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .transform(new GlideRoundTransform(radius)))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory
                                .create(CommApplication.Companion.getInstance().getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(radius);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        loadRoundTransform(imageView.getContext(), errorDrawable, imageView, radius);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        if (imageView.getDrawable() == null) {
                            loadRoundTransform(imageView.getContext(), placeholder, imageView, radius);
                        }
                    }
                });

    }


    /**
     * 加载圆角图
     */
    public void loadRoundImg(ImageView imageView, String url, int placeholderDrawable,
                             int errorDrawable, int radius) {

        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .transform(new MultiTransformation(new CenterCrop(), new GlideRoundTransform(radius))))
                .thumbnail(loadTransform(imageView.getContext(), placeholderDrawable, radius))
                .thumbnail(loadTransform(imageView.getContext(), errorDrawable, radius))
                .into(imageView);
    }

    /**
     * 加载圆角图
     */
    public void loadRoundImg(ImageView imageView, String url, int radius) {

        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .transform(new MultiTransformation(new CenterCrop(), new GlideRoundTransform(radius))))
                .into(imageView);
    }

    private static RequestBuilder<Drawable> loadTransform(Context context, @DrawableRes int placeholderId, int radius) {

        return Glide.with(context)
                .load(placeholderId)
                .apply(new RequestOptions().centerCrop()
                        .transform(new GlideRoundTransform(radius)));

    }


    private void loadRoundTransform(Context context, Drawable placeholderId, ImageView imageView, int radius) {
        Glide.with(context)
                .asDrawable()
                .load(placeholderId)
                .apply(new RequestOptions()
                        .transform(new GlideRoundTransform(radius))
                        .centerCrop())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                    }
                });

    }

    /**
     * 把网络资源图片转化成bitmap
     *
     * @param url 网络资源图片
     * @return Bitmap
     */
    public static Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }

    /**
     * 控件已固定宽度，控件的高度由图片尺寸比例控制显示
     * <p>
     * isErrorSquare:加载异常显示默认图片时，true:为正方形 false:为16:9
     * imageViewWidth:传入view的宽度，若layoutParams.width是有效值则传-1即可
     */
    public void loadImgBySize(ImageView imageView, String url, Drawable placeholderDrawable,
                              Drawable errorDrawable, boolean isErrorSquare, int imageViewWidth) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(placeholderDrawable)
                        .error(errorDrawable)
                        .centerCrop()
                )
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        int viewWidth;
                        if (imageViewWidth == -1) {
                            viewWidth = layoutParams.width;
                        } else {
                            viewWidth = imageViewWidth;
                        }
                        int viewHeight = height * viewWidth / width;
                        layoutParams.height = viewHeight;
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                        int viewWidth;
                        if (imageViewWidth == -1) {
                            viewWidth = layoutParams.width;
                        } else {
                            viewWidth = imageViewWidth;
                        }
                        if (isErrorSquare) {
                            layoutParams.height = viewWidth;
                        } else {
                            layoutParams.height = (viewWidth * 9) / 16;
                        }
                        imageView.setLayoutParams(layoutParams);
                        imageView.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);

                    }
                });
    }

}
