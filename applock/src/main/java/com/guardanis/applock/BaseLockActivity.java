package com.guardanis.applock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;

public abstract class BaseLockActivity extends AppCompatActivity {

    public static final String INTENT_OVERRIDE_VIEW_COUNT = "intent_input_views_count";
    public static final String INTENT_DISPLAY_CHARACTERS_AS_PASSWORD = "intent_characters_as_password";

    protected PINInputController inputController;
    protected PINInputView pinInputView;
    protected TextView descriptionView;

    protected int inputViewsCount = 4;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__main);
        setup();
    }

    protected void setup() {
        this.inputViewsCount = getIntent().getIntExtra(INTENT_OVERRIDE_VIEW_COUNT, getResources().getInteger(R.integer.pin__default_input_count));
        this.descriptionView = (TextView) findViewById(R.id.pin__description);
        this.pinInputView = (PINInputView) findViewById(R.id.pin__input_view);

        this.inputController = new PINInputController(pinInputView, null)
                .setInputNumbersCount(inputViewsCount)
                .setPasswordCharactersEnabled(getIntent().getBooleanExtra(INTENT_DISPLAY_CHARACTERS_AS_PASSWORD, 
                        getResources().getBoolean(R.bool.pin__default_item_password_chars_enabled)));
    }
}
