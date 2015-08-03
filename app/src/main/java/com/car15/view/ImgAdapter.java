package com.car15.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by 思宁 on 2015/8/1.
 * 图片轮播相关
 */
public class ImgAdapter extends BaseAdapter {

    private Context _context;
    private List<Drawable> imgList;
    public ImgAdapter(Context context,List<Drawable> imgList ) {
        _context = context;
        this.imgList=imgList;
    }

    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public Object getItem(int position) {

        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            ImageView imageView = new ImageView(_context);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(
                    Gallery.LayoutParams.FILL_PARENT, Gallery.LayoutParams.WRAP_CONTENT));
            convertView = imageView;
            viewHolder.imageView = (ImageView) convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imageView.setImageDrawable(imgList.get(position % imgList.size()));
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
    }
}
