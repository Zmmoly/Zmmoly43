package com.zamouli.aiassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizer
import android.util.Log

/**
 * معرّف التعرف على الكلام
 * يستخدم واجهة Android SpeechRecognizer الرسمية
 * تم تصميمه ليتوافق مع استخدامه في MainActivity
 */
class SpeechRecognizer(
    private val context: Context,
    private val callback: SpeechRecognizerCallback
) {
    private var androidSpeechRecognizer: AndroidSpeechRecognizer? = null
    
    companion object {
        private const val TAG = "SpeechRecognizer"
    }
    
    /**
     * واجهة الاستدعاء لنتائج التعرف على الكلام
     */
    interface SpeechRecognizerCallback {
        fun onResult(text: String)
        fun onError(errorMessage: String)
    }
    
    /**
     * بدء الاستماع للكلام
     */
    fun startListening() {
        if (!AndroidSpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError("التعرف على الكلام غير متاح على هذا الجهاز")
            return
        }
        
        androidSpeechRecognizer = AndroidSpeechRecognizer.createSpeechRecognizer(context)
        
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // جاهز للاستماع
            }

            override fun onBeginningOfSpeech() {
                // بدء الكلام
            }

            override fun onRmsChanged(rmsdB: Float) {
                // تغير مستوى الصوت
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // لم يتم تنفيذ معالجة خاصة
            }

            override fun onEndOfSpeech() {
                // انتهاء الكلام
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorMessage(error)
                callback.onError(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    callback.onResult(matches[0])
                } else {
                    callback.onError("لم يتم التعرف على أي كلام")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // نتائج جزئية - يمكن تنفيذها لاحقاً
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // أحداث أخرى - لا حاجة للتنفيذ حالياً
            }
        }

        androidSpeechRecognizer?.setRecognitionListener(recognitionListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar_SA")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000)
        }
        
        try {
            androidSpeechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في بدء الاستماع: ${e.message}", e)
            callback.onError("فشل بدء الاستماع: ${e.message}")
        }
    }
    
    /**
     * إيقاف الاستماع
     */
    fun stopListening() {
        try {
            androidSpeechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في إيقاف الاستماع: ${e.message}", e)
        }
    }
    
    /**
     * تحرير الموارد
     */
    fun release() {
        try {
            androidSpeechRecognizer?.destroy()
            androidSpeechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في تحرير الموارد: ${e.message}", e)
        }
    }
    
    /**
     * تحويل رمز الخطأ إلى رسالة نصية
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            AndroidSpeechRecognizer.ERROR_AUDIO -> "خطأ في المدخلات الصوتية"
            AndroidSpeechRecognizer.ERROR_CLIENT -> "خطأ في جانب العميل"
            AndroidSpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "تصاريح غير كافية"
            AndroidSpeechRecognizer.ERROR_NETWORK -> "خطأ في الشبكة"
            AndroidSpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "انتهت مهلة الشبكة"
            AndroidSpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على الكلام"
            AndroidSpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "معرف الكلام مشغول"
            AndroidSpeechRecognizer.ERROR_SERVER -> "خطأ في الخادم"
            AndroidSpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة الكلام"
            else -> "خطأ غير معروف: $errorCode"
        }
    }
}