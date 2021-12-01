package io.github.dawncraft.desktopaddons.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import io.github.dawncraft.desktopaddons.DAApplication;
import io.github.dawncraft.desktopaddons.R;
import io.github.dawncraft.desktopaddons.model.UserModel;
import io.github.dawncraft.desktopaddons.util.Utils;

public class LoginFragment extends Fragment
{
    private CoordinatorLayout layoutLogin;
    private EditText editTextAccount;
    private EditText editTextPassword;

    private NavController navController;

    private UserModel userModel;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        userModel = new UserModel();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        layoutLogin = root.findViewById(R.id.layoutLogin);
        editTextAccount = root.findViewById(R.id.editTextAccount);
        editTextPassword = root.findViewById(R.id.editTextPassword);
        TextView textViewForget = root.findViewById(R.id.textViewForget);
        textViewForget.setText(getForgetPasswordString());
        // NOTE 不加下面这行则无法点击TextView里的超链接
        textViewForget.setMovementMethod(LinkMovementMethod.getInstance());
        Button buttonLogin = root.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ProgressDialog progressDialog = ProgressDialog.show(getContext(),
                        "", getString(R.string.logging_in), true);
                String username = editTextAccount.getText().toString();
                String password = editTextPassword.getText().toString();
                userModel.asyncLogin(username, password, new UserModel.OnLoginListener()
                {
                    @Override
                    public void onLoginResult(UserModel.LoginResult result)
                    {
                        layoutLogin.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                if (result == UserModel.LoginResult.SUCCESS)
                                {
                                    DAApplication.getPreferences().edit()
                                            .putString("username", username)
                                            .putString("password", password)
                                            .apply();
                                    navController.navigateUp();
                                    return;
                                }
                                showMessage(result.getMessage());
                            }
                        });
                    }
                });
            }
        });
        ImageButton imageButtonQQ = root.findViewById(R.id.buttonLoginQQ);
        imageButtonQQ.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Utils.toast(getContext(), "下次一定(逃");
            }
        });
        ImageButton imageButtonWechat = root.findViewById(R.id.buttonLoginWeChat);
        imageButtonWechat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Utils.toast(getContext(), "这辈子大概都不会支持微信的吧");
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        String account = DAApplication.getPreferences().getString("username", null);
        if (account != null) editTextAccount.setText(account);
    }

    private SpannableString getForgetPasswordString()
    {
        String stringForget = getString(R.string.forget_password);
        String stringFind = getString(R.string.find_password);
        SpannableString spannableString = new SpannableString(stringForget);
        int startIndex = stringForget.indexOf(stringFind);
        int endIndex = startIndex + stringFind.length();
        spannableString.setSpan(new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {
                Utils.openUrl(requireContext(), "https://h5.dawncraft.cc/forgetPassword");
            }

            @Override
            public void updateDrawState(TextPaint ds)
            {
                ds.setColor(ds.linkColor);
            }
        }, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void showMessage(@StringRes int message)
    {
        Snackbar.make(layoutLogin, message, Snackbar.LENGTH_LONG)
                .show();
    }
}
