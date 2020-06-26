package com.example.touristslens;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRecycler extends RecyclerView.Adapter<ImageViewHolder>{

    private Context mContext;
    private List<ImageInfo> mImageInfo;
    private int mLastPos= -1;

    public AdapterRecycler(Context mContext, List<ImageInfo> mImageInfo) {
        this.mContext = mContext;
        this.mImageInfo = mImageInfo;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.image_card_view,parent,false);

        return new ImageViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

       //mImageInfo.get(position).getImageData();

        holder.mMonument.setImageURI(mImageInfo.get(position).getImageData());
        holder.mLabel.setText(mImageInfo.get(position).getLabel());
        holder.mDate.setText(mImageInfo.get(position).getDate());

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**Redirect it to activity where Image+info is stored*/
                Intent intent = new Intent(mContext,MonumentInfoActivity.class);
                intent.putExtra("Monument",mImageInfo.get(position).getImageData().toString());
                intent.putExtra("Label",mImageInfo.get(position).getLabel());
                intent.putExtra("FromActivity",MainPageActivity.class.getName());
                mContext.startActivity(intent);
            }
        });

        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                MainPageActivity mainPageActivity = (MainPageActivity) mContext;

                //enable delete/share here
                mainPageActivity.toggleDeleteMenuItem(true);

                ColorFilter cf1 = holder.mMonument.getColorFilter();
                if(cf1 == null)
                {
                    mainPageActivity.uri_path.add(mImageInfo.get(position).getImageData().toString());

                    //give a black tint indicating the image is selected
                    holder.mMonument.setColorFilter(Color.argb((float) 0.5,0,0,0));
                    Log.e("positionList",mainPageActivity.uri_path.size()+""); //debugging
                }
                else {
                    Log.e("colorfilter",cf1.toString());

                    mainPageActivity.uri_path.remove( mImageInfo.get(position).getImageData().toString());
                    holder.mMonument.setColorFilter(null);

                    Log.e("Sizeofuripath:",""+mainPageActivity.uri_path.size());

                    if(mainPageActivity.uri_path.size() == 0)
                    {
                        mainPageActivity.toggleDeleteMenuItem(false);
                    }
                }

                return true;
            }
        });

        setAnim(holder.itemView,position);
    }

    private void setAnim(View anim,int position)
    {
        if(position > mLastPos)
        {
            ScaleAnimation scaleAnimation  = new ScaleAnimation(1.0f,1.0f,0.1f,1.0f,
                    Animation.RELATIVE_TO_SELF,0.5f,
                    Animation.RELATIVE_TO_SELF,0.5f);
            scaleAnimation.setDuration(600);
            anim.startAnimation(scaleAnimation);
            mLastPos = position;
        }
    }
    @Override
    public int getItemCount() {
        return mImageInfo.size();
    }
}
class ImageViewHolder extends RecyclerView.ViewHolder
{
    ImageView mMonument;
    TextView mLabel,mDate;
    CardView mCardView;
    public ImageViewHolder( View itemView) {
        super(itemView);
        mMonument = itemView.findViewById(R.id.card_image);
        mLabel = itemView.findViewById(R.id.card_label );
        mDate = itemView.findViewById(R.id.card_date);
        mCardView = itemView.findViewById(R.id.cardview);
    }
}
