package com.sa.keyboard;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

public class KeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean isCap = false;
    private boolean isLongCap = false;
    private Context context = this;
    private long lastPressTime;
    private final long longPressTime = 500;
    private int shiftClick = 0;


//        Toast.makeText(this,"set",Toast.LENGTH_SHORT).show();

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard_default, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setPreviewEnabled(false);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
//        for first letter capital
        isCap = true;
        setShiftIcon(Keyboard.KEYCODE_SHIFT);
        keyboard.setShifted(isCap);
        kv.invalidateAllKeys();
        return kv;
    }

    @Override
    public void onPress(int primaryCode) {
        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            lastPressTime = System.currentTimeMillis();
        }
        if (primaryCode == -1 || primaryCode == -2 || primaryCode == -5 || primaryCode == -4 || primaryCode == 32) {
            kv.setPreviewEnabled(false);
        } else {
            kv.setPreviewEnabled(true);
        }
    }

    /**
     * this method used for detect long press
     *
     * @param current
     * @return
     */
    private boolean isLongPress(long current) {
        return current - lastPressTime > longPressTime;
    }

    private void ShiftDoublePress() {
        shiftClick++;
    }

    @Override
    public void onRelease(int primaryCode) {
        kv.setPreviewEnabled(false);
//        for shift key
        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            boolean isLongPress = isLongPress(System.currentTimeMillis());
            if (this.isLongCap) {
                isCap = false;
                isLongCap = false;
                keyboard.setShifted(false);
            } else if (isLongPress) {
                isLongCap = true;
                isCap = false;
                keyboard.setShifted(true);
            } else {
                ShiftDoublePress();
                if (shiftClick == 2) {
                    isCap = false;
                    shiftClick = 0;
                    isLongCap = true;
                    keyboard.setShifted(true);
                } else {
                    isCap = !isCap;
                    keyboard.setShifted(isCap);
                }
            }

            kv.invalidateAllKeys();
            setShiftIcon(primaryCode);
        }
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();

        CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);

        if (lastTwo.length() == 0) {
            isCap = true;
            setShiftIcon(Keyboard.KEYCODE_SHIFT);
            keyboard.setShifted(isCap);
            kv.invalidateAllKeys();
        }

        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    ic.deleteSurroundingText(1, 0);
                } else {
                    ic.commitText("", 1);
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:

                char code = (char) primaryCode;
                if (Character.isLetter(code) && kv.isShifted()) {
                    code = Character.toUpperCase(code);
//                    remove caps
                    if (isCap) {
                        shiftClick = 0;
                        isCap = false;
                        setShiftIcon(Keyboard.KEYCODE_SHIFT);
                        keyboard.setShifted(isCap);
                        kv.invalidateAllKeys();
                    }
                }
                ic.commitText(String.valueOf(code), 1);
        }
    }

    private void playClick(int primaryCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (primaryCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    /**
     * this method is used for set icon for the shift key
     *
     * @param primaryCode
     */
    private void setShiftIcon(int primaryCode) {
        Keyboard currentKeyboard = kv.getKeyboard();
        List<Keyboard.Key> keys = currentKeyboard.getKeys();
        kv.invalidateKey(primaryCode);

        for (int i = 0; i < keys.size() - 1; i++) {
            Keyboard.Key currentKey = keys.get(i);

            //If your Key contains more than one code, then you will have to check if the codes array contains the primary code
            if (currentKey.codes[0] == primaryCode) {
                currentKey.label = null;

                int icon = isCap ? R.drawable.ic_shift_click : R.drawable.ic_shift;

                if (isLongCap)
                    icon = R.drawable.ic_shift_double;

                currentKey.icon = getResources().getDrawable(icon);
                break; // leave the loop once you find your match
            }
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
