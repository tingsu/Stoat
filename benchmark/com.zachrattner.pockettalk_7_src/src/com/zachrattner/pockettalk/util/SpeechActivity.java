/******************************************************************************
 * This file is part of Pocket Talk, an Android application that reads text 
 * messages aloud, and vibrates them in International Morse Code.
 *
 * Pocket Talk was released in January 2012 by Zach Rattner 
 * (info@zachrattner.com).
 *
 * Pocket Talk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pocket Talk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pocket Talk.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.zachrattner.pockettalk.util;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.widget.Toast;

/* An abstract class that simplifies activities that have the ability to
 * perform text-to-speech. */
public abstract class SpeechActivity extends Activity implements TextToSpeech.OnInitListener
{
	/* Log tag, for debugging */
    private static final String TAG = SpeechActivity.class.getCanonicalName();

    /* Utterance ID for text-to-speech engine */
    private static final String UTTERANCE_ID = "Utterance";
    
    /* Reference to self, for anonymous inner classes */
    protected Activity m_GUI;
    
    /* Audio manager, for muting music track during speech */
    protected AudioManager m_AudioManager;
    
    /* Interface to text-to-speech engine */
    protected TextToSpeech m_TextToSpeech;
    
    /* Application preferences */
    protected Preferences m_Preferences;

    @Override
    public void onCreate(Bundle State)
    {
        super.onCreate(State);

        m_GUI          = this;
        m_TextToSpeech = new TextToSpeech(this, this);
        m_Preferences  = new Preferences(this);
        
        m_AudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }
    
    /* Free the text-to-speech resource before destroying */
    @Override
    public void onDestroy()
    {
        if (m_TextToSpeech != null)
        {
        	m_TextToSpeech.stop();
        	m_TextToSpeech.shutdown();
        }

        super.onDestroy();
    }
    
	public void onInit(int Status)
	{
		/* Ensure that the text-to-speech engine is active before
		 * continuing. */
        if (Status != TextToSpeech.SUCCESS)
        {
        	handleInitFailure();
            return;
        }
        
        /* Attempt to load the default voice */
        Locale Language = Locale.getDefault();
        if (Language == null)
        {
        	Log.w(TAG, "No default language, using US English");
        	Language = Locale.US;
        }
        
        /* If the voice failed, then give up. */
        int Result = m_TextToSpeech.setLanguage(Language);
        if ((Result == TextToSpeech.LANG_MISSING_DATA) ||
            (Result == TextToSpeech.LANG_NOT_SUPPORTED))
        {
        	handleInitFailure();
            return;
        }
        
        /* Load the string to speak. */
        String ToSpeak = m_Preferences.speechText();
        if ((ToSpeak != null) && (ToSpeak.length() > 0))
        {        	
        	/* Handle audio track once the speech is complete. */
        	m_TextToSpeech.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
        	{
				public void onUtteranceCompleted(String UtteranceID)
				{
					Log.d(TAG, "Utterance complete!");
					m_AudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
					m_GUI.finish();
				}
        	});
        	
        	/* Set parameters */
        	HashMap<String, String> Parameters = new HashMap<String, String>();
        	
        	/* Use default utterance ID. */
        	Parameters.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        	
        	/* Use the voice call audio stream. */
        	Parameters.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_VOICE_CALL));
        	
        	/* Mute the music channel. */
        	m_AudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        	
        	/* Speak the text. */
        	m_TextToSpeech.speak(ToSpeak, TextToSpeech.QUEUE_ADD, Parameters);
        	
        	/* Clear the text to prevent it from being spoken again. */
        	m_Preferences.clearSpeechText();
        }
	}
	
	/* Handle failure if the text-to-speech engine could not be initialized. */
	private void handleInitFailure()
	{
	    Log.e(TAG, "Text to speech is not available.");
	    Toast.makeText(this, "Text to speech is unavailable.", Toast.LENGTH_SHORT).show();	
	}
}
