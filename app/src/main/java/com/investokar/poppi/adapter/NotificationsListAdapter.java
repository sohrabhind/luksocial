package com.investokar.poppi.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.investokar.poppi.R;
import com.investokar.poppi.constants.Constants;
import com.investokar.poppi.model.Chat;
import com.investokar.poppi.model.Notify;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;



public class NotificationsListAdapter extends RecyclerView.Adapter<NotificationsListAdapter.ViewHolder> implements Constants {

    private final Context ctx;
    private final List<Notify> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Notify item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, time;
        public CircularImageView image, online;
        public ImageView mProfileLevelIcon, icon;
        public LinearLayout parent;
        public TextView message;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            message = view.findViewById(R.id.message);
            time = view.findViewById(R.id.time);
            image = view.findViewById(R.id.image);
            parent = view.findViewById(R.id.parent);

            online = view.findViewById(R.id.online);
            mProfileLevelIcon = view.findViewById(R.id.profileLevelIcon);
            icon = view.findViewById(R.id.icon);
        }
    }

    public NotificationsListAdapter(Context mContext, List<Notify> items) {
        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final Notify item = items.get(position);

        holder.online.setVisibility(View.GONE);
        holder.mProfileLevelIcon.setVisibility(View.GONE);
        
        final ImageView img = holder.image;
        if (item.getFromUserPhotoUrl() != null && !item.getFromUserPhotoUrl().trim().isEmpty() && item.getFromUserId() != 0) {
			try {
                Picasso.get()
                        .load(item.getFromUserPhotoUrl())
                        .placeholder(R.drawable.profile_default_photo)
                        .error(R.drawable.profile_default_photo)
                        .into(holder.image, new Callback() {
                            @Override
                            public void onSuccess() {
                                img.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                img.setImageResource(R.drawable.profile_default_photo);
                                img.setVisibility(View.VISIBLE);
                            }
                        });

            } catch (Exception e) {
                Log.e("NotifyListAdapter", e.toString());
            }
		} else {
			img.setImageResource(R.drawable.profile_default_photo);
		}
        
        String fullname = item.getFromUserFullname();
		if (fullname.split("\\w+").length > 1) {
            fullname = fullname.substring(0, fullname.lastIndexOf(' '));
        }
        if (item.getFromUserId() != 0) {
            holder.title.setText(fullname);
        } else {
            holder.image.setImageResource(R.drawable.ic_action_liked);
            holder.title.setText(ctx.getString(R.string.app_name));
        }

        if (item.getType() == NOTIFY_TYPE_LIKE) {
            holder.message.setText(fullname + " " + ctx.getText(R.string.label_likes_profile));
            holder.icon.setImageResource(R.drawable.ic_action_liked);
         } else if (item.getType() == NOTIFY_TYPE_IMAGE_LIKE) {
            holder.message.setText(fullname + " " + ctx.getText(R.string.label_likes_item));
            holder.icon.setImageResource(R.drawable.ic_action_liked);
        } else if (item.getType() == NOTIFY_TYPE_MEDIA_APPROVE) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_media_approved), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_action_done);
        } else if (item.getType() == NOTIFY_TYPE_MEDIA_REJECT) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_media_rejected), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_rejected);
        } else if (item.getType() == NOTIFY_TYPE_ACCOUNT_APPROVE) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_approved_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_action_done);
        } else if (item.getType() == NOTIFY_TYPE_ACCOUNT_REJECT) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_rejected);
        } else if (item.getType() == NOTIFY_TYPE_PROFILE_PHOTO_APPROVE) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_approved_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_action_done);
        } else if (item.getType() == NOTIFY_TYPE_PROFILE_PHOTO_REJECT) {
            holder.message.setText(String.format(Locale.getDefault(), ctx.getString(R.string.label_profile_photo_rejected_new), ctx.getString(R.string.app_name)));
            holder.icon.setImageResource(R.drawable.ic_rejected);
        } else {
            holder.message.setText(fullname + " " + ctx.getText(R.string.label_friend_request_added));
            holder.icon.setImageResource(R.drawable.ic_action_done);
        }

        holder.time.setText(item.getTimeAgo());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Notify item = items.get(position);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, item, position);
                }
            }
        });

    }

    public Notify getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnClickListener {
        void onItemClick(View view, Chat item, int pos);
    }
}