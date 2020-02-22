package com.pillpals.pillpals.ui.quiz

import com.pillpals.pillpals.data.model.Medications
import com.pillpals.pillpals.data.model.Questions
import io.realm.RealmList
import java.io.IOException


fun generateQuestion(id: Int, medication: Medications):Questions {
    var question = Questions()
    question.medication = medication
    var correctAnswerString = ""
    var incorrectAnswers = mutableListOf<String>()

    when (id) {
        //Questions with a medication that requires a linked database drug
        1-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 1"
        }
        2-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 2"
        }
        3-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 3"
        }
        4-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 4"
        }
        5-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 5"
        }


        //Questions that can be used on any medication
        101-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 101"
        }
        102-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 102"
        }
        103-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 103"
        }
        104-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 104"
        }
        105-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 105"
        }
        106-> {
            correctAnswerString = "Correct"
            incorrectAnswers.add("Incorrect 1")
            incorrectAnswers.add("Incorrect 2")
            incorrectAnswers.add("Incorrect 3")
            question.question = "This question is asking about " + medication.name + " using template 105"
            throw IOException("question failed to generate")
        }

    }

    //if there are more than 3 incorrect answers, remove some
    incorrectAnswers.shuffle()
    incorrectAnswers = incorrectAnswers.take(3) as MutableList<String>

    //randomize Answers
    incorrectAnswers.add(correctAnswerString)
    incorrectAnswers.shuffle()
    for (i in 0..3) {
        question.answers.add(incorrectAnswers[i])
        if (correctAnswerString == incorrectAnswers[i]) {
            question.correctAnswer = i
        }
    }

    return question
}
