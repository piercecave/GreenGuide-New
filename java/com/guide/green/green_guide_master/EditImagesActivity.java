package com.guide.green.green_guide_master;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.guide.green.green_guide_master.Dialogs.LoadingDialog;
import com.guide.green.green_guide_master.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide_master.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide_master.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide_master.HTTPRequest.POSTMultipartData;
import com.guide.green.green_guide_master.Utilities.CredentialManager;
import com.guide.green.green_guide_master.Utilities.Review;

import org.json.JSONArray;

import java.util.ArrayList;

public class EditImagesActivity extends AppCompatActivity {

    private Review mReview = new Review();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_images);

        mReview = CredentialManager.getMyReview();

        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        String reviewId = String.valueOf(Integer.parseInt(mReview.id));
        formItems.add(new AbstractFormItem.TextFormItem("id", reviewId));
        formItems.add(new AbstractFormItem.TextFormItem("img_token", "02242019work"));

        Toast.makeText(this, "Retrieving images... " + reviewId, Toast.LENGTH_LONG).show();

        final LoadingDialog ld = new LoadingDialog();
        ld.show(getFragmentManager(), "Deleting Review...");

        final Activity mAct = this;

        final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                "http://www.lovegreenguide.com/img_e_app.php", formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                    @Override
                    public void onSuccess(StringBuilder stringBuilder) {
                        ld.dismiss();
                        Log.d("ReviewXYZ: ", stringBuilder.toString());

                        StringBuilder sb = stringBuilder;

                        JSONArray jArr = null;
                        JSONArray jArr_types = null;
                        try {
                            int startingIndex = sb.toString().indexOf("[", sb.toString().indexOf("[") + 1);
                            int endingIndex = sb.toString().indexOf("]", sb.toString().indexOf("]") + 1) + 1;
                            jArr = new JSONArray(sb.toString().substring(0, startingIndex));

                            int startingIndex_2 = sb.toString().indexOf("[", sb.toString().indexOf("[", endingIndex - 1));
                            int endingIndex_2 = sb.toString().indexOf("]", sb.toString().indexOf("]", endingIndex )) + 1;
                            jArr_types = new JSONArray(sb.toString().substring(startingIndex_2, endingIndex_2));

//                            for (int i=0; i < jArr.length(); i++){
//                                JSONObject jsonObject = new JSONObject(jArr.get(i).toString());
//                                jsonObject.put("STATUSUPDATE", "1");
//                                jArr.put(i, jsonObject);
//                            }

                            Log.d("ARRAY_X: ", jArr.toString());
                            Log.d("ARRAY_X_Y: ", jArr_types.toString());
                            //JSONObject urlsJSON = jArr.getJSONObject(0);
                            //Log.d("YYY: ", urlsJSON.toString());
                        } catch (Exception e) {
                            Log.d("edit exception: ", e.getMessage());
                        }
                        ArrayList<String> imageUrls = new ArrayList<String>();

                        for (int i = 0; i < jArr.length(); i++) {
                            try {
                                imageUrls.add(jArr.getString(i));
                            } catch (Exception e) {
                            }
                        }

//                        findViewById(R.id.reviewImages_progress).setVisibility(View.GONE);
//
//                        RecyclerView mRecycleView = findViewById(R.id.reviewImages);
//
//                        mRecycleView.setVisibility(View.VISIBLE);
//
//                        RecyclerView.Adapter<PictureCarouselAdapter.CarouselViewHolder> adapter = new
//                                PictureCarouselAdapter(mAct.getApplicationContext(), imageUrls);
//                        mRecycleView.setAdapter(adapter);
//                        mRecycleView.setHasFixedSize(true);
//
//                        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mAct,
//                                LinearLayoutManager.HORIZONTAL, false);
//
//                        mRecycleView.setLayoutManager(mLayoutManager);

                        final LinearLayout editImagesLayout = findViewById(R.id.edit_images_root);

                        for (String url : imageUrls) {
                            Log.d("IMG", "IMAGE URL: " + url);
                            AsyncRequest.getImage("http://www.lovegreenguide.com/" + url,
                                    new AbstractRequest.OnRequestResultsListener<Bitmap>() {
                                        @Override
                                        public void onSuccess(Bitmap bitmap) {
                                            //ImageView newImage = new ImageView(getApplicationContext());
                                            //newImage.setImageBitmap(bitmap);
                                            //editImagesLayout.addView(newImage);

                                            final ViewGroup parent = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                                                    R.layout.write_review_selected_image_item, null);

                                            ((ImageView) parent.findViewById(R.id.selected_image)).setImageBitmap(bitmap);
                                            parent.findViewById(R.id.remove_selected_image).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    editImagesLayout.removeView(parent);
                                                    ArrayList<AbstractFormItem> formItems = new ArrayList<>();
                                                    String reviewId = String.valueOf(Integer.parseInt(mReview.id));
                                                    formItems.add(new AbstractFormItem.TextFormItem("id", reviewId));
                                                    formItems.add(new AbstractFormItem.TextFormItem("s_name", CredentialManager.getUsername()));
                                                    formItems.add(new AbstractFormItem.TextFormItem("del_token", "02042019work"));

                                                    Toast.makeText(getApplicationContext(), "Deleting Review... " + reviewId, Toast.LENGTH_LONG).show();
                                                    final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                                                            "http://www.lovegreenguide.com/del_app.php", formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                                                                @Override
                                                                public void onSuccess(StringBuilder stringBuilder) {
                                                                    ld.dismiss();
                                                                    Toast.makeText(getApplicationContext(), "Review: " + stringBuilder.toString() + "deleted", Toast.LENGTH_LONG).show();
                                                                }});
                                                }
                                            });

                                            editImagesLayout.addView(parent);
                                        }

                                        @Override
                                        public void onError(Exception error) {
                                            Log.i("--onError_CarouselPic", error.toString());
                                            error.printStackTrace();
                                        }
                                    });
                        }
                    }
                });

        ld.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                postRequest.cancel(true);
            }
        });
    }
}
