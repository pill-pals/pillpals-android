package com.pillpals.pillpals.helpers

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.pillpals.pillpals.data.model.*
import io.realm.RealmResults
import java.util.*
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Environment
import androidx.core.app.ActivityCompat.requestPermissions
import java.io.*
import java.io.FileWriter
import java.nio.file.Files.exists
import android.os.Environment.getExternalStorageDirectory








class FileWriter {
    companion object {
        fun writeToFile(data: String, context: Context) {
            val extStorageDirectory = context.getExternalFilesDir(null).toString()
            val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val file = File(extStorageDirectory, "pillpals-$id.json")

            var bos: BufferedOutputStream? = null

            try {
                val fos = FileOutputStream(file)
                bos = BufferedOutputStream(fos)
                bos.write(data.toByteArray())
            }
            finally {
                if (bos != null) {
                    try {
                        bos.flush ();
                        bos.close ();
                    }
                    catch (e: Exception) {
                    }
                }
            }
        }

        fun createJSONStringFromData(context: Context) {
            val logsOutputList = (DatabaseHelper.readAllData(Logs::class.java) as RealmResults<out Logs>)
                .fold(listOf<LogsOutput>()) { acc, it ->
                    acc.plus(LogsOutput(it.uid, it.due, it.occurrence, it.schedule?.firstOrNull()?.uid))
                }

            val moodLogsOutputList = (DatabaseHelper.readAllData(MoodLogs::class.java) as RealmResults<out MoodLogs>)
                .fold(listOf<MoodLogsOutput>()) { acc, it ->
                    acc.plus(MoodLogsOutput(it.uid, it.date, it.rating))
                }

            val scheduleOutputList = (DatabaseHelper.readAllData(Schedules::class.java) as RealmResults<out Schedules>)
                .fold(listOf<ScheduleOutput>()) { acc, it ->
                    acc.plus(ScheduleOutput(
                        it.uid,
                        it.medication?.firstOrNull()?.uid,
                        it.startDate,
                        it.repetitionCount,
                        it.repetitionUnit,
                        it.deleted,
                        it.deletedDate,
                        it.logs?.fold(listOf<String?>()) {innerAcc, inner ->
                            innerAcc.plus(inner.uid)
                        }
                    ))
                }

            val medicationOutputList = (DatabaseHelper.readAllData(Medications::class.java) as RealmResults<out Medications>)
                .fold(listOf<MedicationOutput>()) { acc, it ->
                    acc.plus(MedicationOutput(
                        it.uid,
                        it.deleted,
                        it.photo_icon,
                        it.icon_id,
                        it.color_id,
                        it.schedules?.fold(listOf<String?>()) {innerAcc, inner ->
                            innerAcc.plus(inner.uid)
                        },
                        it.dpd_object?.firstOrNull() != null
                    ))
                }

            val quizzesOutputList = (DatabaseHelper.readAllData(Quizzes::class.java) as RealmResults<out Quizzes>)
                .fold(listOf<QuizzesOutput>()) { acc, it ->
                    acc.plus(QuizzesOutput(
                        it.uid,
                        it.date,
                        it.questions?.fold(listOf<String?>()) {innerAcc, inner ->
                            innerAcc.plus(inner.uid)
                        }
                    ))
                }

            val questionsOutputList = (DatabaseHelper.readAllData(Questions::class.java) as RealmResults<out Questions>)
                .fold(listOf<QuestionsOutput>()) { acc, it ->
                    acc.plus(QuestionsOutput(
                        it.uid,
                        it.correctAnswer,
                        it.userAnswer,
                        it.medication?.uid,
                        it.template?.firstOrNull()?.id,
                        it.quiz?.firstOrNull()?.uid
                    ))
                }

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val settingsPin = sharedPreferences.getString("pin", "")

            val outputObject = FileOutput(
                logs = logsOutputList,
                mood_logs = moodLogsOutputList,
                schedules = scheduleOutputList,
                medications = medicationOutputList,
                quizzes = quizzesOutputList,
                questions = questionsOutputList,
                pin = settingsPin
            )

            val outputJson: String = Gson().toJson(outputObject)

            writeToFile(outputJson, context)
        }
    }
}

data class LogsOutput(
    val uid: String?,
    val due: Date?,
    val occurrence: Date?,
    val schedule_uid: String?
)

data class MoodLogsOutput(
    val uid: String?,
    val date: Date?,
    val rating: Int?
)

data class ScheduleOutput(
    val uid: String?,
    val medication_uid: String?,
    val startDate: Date?,
    val repetitionCount: Int?,
    val repetitionUnit: Int?,
    val deleted: Boolean?,
    val deletedDate: Date?,
    val log_uids: List<String?>?
)

data class MedicationOutput(
    val uid: String,
    val deleted: Boolean?,
    val photo_icon: Boolean?,
    val icon_id: Int?,
    val color_id: Int?,
    val schedule_uids: List<String?>?,
    val has_dpd_object: Boolean?
)

data class QuizzesOutput(
    val uid: String,
    val date: Date?,
    val question_uids: List<String?>?
)

data class QuestionsOutput(
    val uid: String,
    val correctAnswer: Int?,
    val userAnswer: Int?,
    val medication_uid: String?,
    val template_id: Int?,
    val quiz_uid: String?
)

data class FileOutput(
    val medications: List<MedicationOutput>,
    val schedules: List<ScheduleOutput>,
    val mood_logs: List<MoodLogsOutput>,
    val logs: List<LogsOutput>,
    val quizzes: List<QuizzesOutput>,
    val questions: List<QuestionsOutput>,
    val pin: String?
)

