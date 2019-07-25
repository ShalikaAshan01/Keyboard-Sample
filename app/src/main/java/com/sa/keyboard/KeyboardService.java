package com.sa.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

public class KeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean isCap = false;

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard_default,null);
        keyboard = new Keyboard(this,R.xml.qwerty);
        kv.setPreviewEnabled(false);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    @Override
    public void onPress(int primaryCode) {
        if (primaryCode==-1||primaryCode==-2||primaryCode==-5||primaryCode==-4 || primaryCode==32){
                kv.setPreviewEnabled(false);
        }else{
            kv.setPreviewEnabled(true);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        kv.setPreviewEnabled(false);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode)
        {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1,0);
                break;
                case Keyboard.KEYCODE_SHIFT:
                    isCap = !isCap;
                    keyboard.setShifted(isCap);
                    kv.invalidateAllKeys();
                    break;
                    case Keyboard.KEYCODE_DONE:
                        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));
                        break;
                        default:
                            char code = (char) primaryCode;
                            if(Character.isLetter(code) && isCap){
                                code = Character.toUpperCase(code);
                            }
                            ic.commitText(String.valueOf(code),1);
        }
    }

    private void playClick(int primaryCode) {
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (primaryCode){
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
