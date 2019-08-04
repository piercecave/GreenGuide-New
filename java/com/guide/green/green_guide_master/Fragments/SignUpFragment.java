package com.guide.green.green_guide_master.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.guide.green.green_guide_master.Dialogs.LoadingDialog;
import com.guide.green.green_guide_master.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide_master.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide_master.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide_master.HTTPRequest.POSTMultipartData;
import com.guide.green.green_guide_master.LogInOutSignUpActivity;
import com.guide.green.green_guide_master.R;

import java.util.ArrayList;

public class SignUpFragment extends AbstractLogInOutSignup {
    private EditText mSignUpUsername;
    private EditText mSignUpPassword;
    private EditText mSignUpPasswordRetype;
    private LoadingDialog ld;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_sign_up, null);
        mSignUpUsername = root.findViewById(R.id.sign_up_username);
        mSignUpPassword = root.findViewById(R.id.sign_up_password);
        mSignUpPasswordRetype = root.findViewById(R.id.sign_up_password_retype);
        root.findViewById(R.id.sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser();
            }
        });
        root.findViewById(R.id.sign_up_goto_log_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchPageTo(LogInOutSignUpActivity.PageOption.LOGIN);
            }
        });
        return root;
    }

    private void signUpUser() {
        String password = mSignUpPasswordRetype.getText().toString();
        if (!mSignUpPassword.getText().toString().equals(password)) {
            Toast.makeText(getContext(), "Password mismatch", Toast.LENGTH_LONG).show();
            return;
        }
        String mTentativeToken = "06232017Job$";
        String mTentativeUsername = mSignUpUsername.getText().toString();
        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        formItems.add(new AbstractFormItem.TextFormItem("email", mTentativeUsername));
        formItems.add(new AbstractFormItem.TextFormItem("password", password));
        formItems.add(new AbstractFormItem.TextFormItem("token_signup", mTentativeToken));

        ld = new LoadingDialog();
        ld.show(getActivity().getFragmentManager(), "Signing Up...");
        final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                "http://www.lovegreenguide.com/register_app.php", formItems, mSignUpHandler);
        ld.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                postRequest.cancel(true);
            }
        });
    }

    private AbstractRequest.OnRequestResultsListener<StringBuilder> mSignUpHandler =
            new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                @Override
                public void onSuccess(StringBuilder stringBuilder) {
                    Log.i("SIGN_UP_SUCCESS>", stringBuilder.toString() + ">" + stringBuilder.toString());
                    Toast.makeText(getContext(),stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    ld.dismiss();
                }

                @Override
                public void onCanceled() {
                    ld.dismiss();
                }

                @Override
                public void onError(Exception error) {
                    error.printStackTrace();
                    ld.dismiss();
                    Toast.makeText(getContext(),"Error Encountered", Toast.LENGTH_LONG).show();
                }
            };
}
