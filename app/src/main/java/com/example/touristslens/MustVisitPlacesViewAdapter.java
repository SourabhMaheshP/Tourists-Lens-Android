package com.example.touristslens;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MustVisitPlacesViewAdapter extends RecyclerView.Adapter<MustVisitPlacesViewHolder>{

    private Context mContext;
    private List<FactsInfo> mFactsInfo;
    private int mLastPos= -1;

    public MustVisitPlacesViewAdapter(Context mContext, List<FactsInfo> mFactsInfo) {
        this.mContext = mContext;
        this.mFactsInfo = mFactsInfo;
    }

    @NonNull
    @Override
    public MustVisitPlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.must_visit_places_view,parent,false);

        return new MustVisitPlacesViewHolder(view) ;
    }

    @Override
    public void onBindViewHolder(@NonNull MustVisitPlacesViewHolder holder, int position) {

        //mFactsInfo.get(position).getImageData();

        String label = mFactsInfo.get(position).getLabel();
        holder.mMonument.setImageDrawable(mFactsInfo.get(position).getImageData());
        holder.mLabel.setText(label);

        holder.mGeoLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //google map intent
                //[Change this Static Code to something logical]
                if(label.equals("TajMahal"))
                    openMaps("27.1751448","78.0399535");
                else if(label.equals("QutubMinar"))
                    openMaps("28.5244754","77.1833319");
                else if(label.equals("IndiaGate"))
                    openMaps(" 28.612912","77.227321");
            }
        });

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("cardview","clicked");
//                /**Redirect it to activity where Image+info is stored*/
                Intent intent = new Intent(mContext,MonumentInfoActivity.class);
                intent.putExtra("Monument","");
                intent.putExtra("Label",mFactsInfo.get(position).getLabel());
                intent.putExtra("FromActivity", MustVisitPlacesActivity.class.getName());
                mContext.startActivity(intent);
            }
        });

            setAnim(holder.itemView,position);
        }
        private void openMaps(String latitude,String longitude)
        {
            Intent map_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+latitude+","+longitude));
            map_intent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(map_intent);
        }

        private void setAnim(View anim,int position)
        {
            if(position > mLastPos)
            {
                ScaleAnimation scaleAnimation  = new ScaleAnimation(0.5f,1.0f,0.5f,1.0f,
                        Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                scaleAnimation.setDuration(1000);
                anim.startAnimation(scaleAnimation);
                mLastPos = position;
            }
        }
    @Override
    public int getItemCount() {
        return mFactsInfo.size();
    }
}
class MustVisitPlacesViewHolder extends RecyclerView.ViewHolder
{
    protected ImageView mMonument;
    protected TextView mLabel;
    protected CardView mCardView;
    protected Button mGeoLoc;

    public MustVisitPlacesViewHolder(View itemView) {
        super(itemView);
        mMonument = itemView.findViewById(R.id.facts_image);
        mLabel = itemView.findViewById(R.id.facts_label );
        mCardView = itemView.findViewById(R.id.facts_cardview);
        mGeoLoc = itemView.findViewById(R.id.geolocation);
    }
}
