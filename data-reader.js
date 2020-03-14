const dataString = `

`;
const dataName = "output"

const sh = require('run-sh');

function convertToCSV(arr) {
  const array = [Object.keys(arr[0])].concat(arr)

  return array.map(it => {
    return Object.values(it).join(";")
  }).join('\n')
}


const data = JSON.parse(dataString);

const pin = [{pin: data.pin}];
const logs = data.logs;
const moodLogs = data.mood_logs;
const schedulesReducer = (accumulator, currentValue) => {
	if(!currentValue.hasOwnProperty("deletedDate")) {
		currentValue = {
			deleted: currentValue.deleted,
		    deletedDate: '',
		    log_uids: currentValue.log_uids,
		    medication_uid: currentValue.medication_uid,
		    repetitionCount: currentValue.repetitionCount,
		    repetitionUnit: currentValue.repetitionUnit,
		    startDate: currentValue.startDate,
		    uid: currentValue.uid
		}
	}
	accumulator.push(currentValue);
	return accumulator
}
const schedules = data.schedules.reduce(schedulesReducer, []);
const medications = data.medications;
const quizzes = data.quizzes;

const questionReducer = (accumulator, currentValue) => {
	if(!currentValue.hasOwnProperty("medication_uid")) {
		currentValue = {
			correctAnswer: currentValue.correctAnswer,
    		medication_uid: "",
    		quiz_uid: currentValue.quiz_uid,
    		template_id: currentValue.template_id,
    		uid: currentValue.uid
		}
	}
	accumulator.push(currentValue);
	return accumulator
}
const questions = data.questions.reduce(questionReducer, []);

const visit_logs = data.visit_logs;

const output = convertToCSV(pin) +
				"\n\n" + convertToCSV(logs) +
				"\n\n" + convertToCSV(moodLogs) +
				"\n\n" + convertToCSV(schedules) +
				"\n\n" + convertToCSV(medications) +
				"\n\n" + convertToCSV(quizzes) +
				"\n\n" + convertToCSV(questions) +
				"\n\n" + convertToCSV(visit_logs);

sh("echo \"" + output +"\" > " + dataName +".csv");
