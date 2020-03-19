const dataString = `
{"logs":[{"due":"Mar 2, 2020 10:30:00 PM","occurrence":"Mar 2, 2020 10:30:09 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"c0fa0e71-c7c9-46ed-9089-aeab14099260"},{"due":"Mar 3, 2020 8:30:00 AM","occurrence":"Mar 3, 2020 8:30:15 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"ed5a8a0b-bf12-4135-affa-ccaa5658db3a"},{"due":"Mar 3, 2020 8:43:00 AM","occurrence":"Mar 3, 2020 8:43:06 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"c620ea3c-1014-4bbe-adbb-87abad8c837a"},{"due":"Mar 3, 2020 10:30:00 PM","occurrence":"Mar 3, 2020 10:34:24 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"6ca084ef-326e-4832-9bba-f12e4e70b78b"},{"due":"Mar 4, 2020 8:30:00 AM","occurrence":"Mar 4, 2020 8:30:09 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"e38fa4e0-f6f3-4755-a475-62598e1b76d8"},{"due":"Mar 4, 2020 8:43:00 AM","occurrence":"Mar 4, 2020 8:43:11 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"cc744ff0-330f-45f1-86ab-fdfe199475e7"},{"due":"Mar 4, 2020 10:30:00 PM","occurrence":"Mar 4, 2020 10:34:24 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"8020f051-5fcd-4ded-9570-05b5c6cf7b52"},{"due":"Mar 5, 2020 8:30:00 AM","occurrence":"Mar 5, 2020 8:34:33 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"f56ad0cb-ee3d-4ee2-8e97-f189f73b1158"},{"due":"Mar 5, 2020 8:43:00 AM","occurrence":"Mar 5, 2020 8:44:25 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"00a0e8f0-f422-46a6-a25e-630b8930c900"},{"due":"Mar 5, 2020 10:30:00 PM","occurrence":"Mar 5, 2020 10:30:47 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"d49f41f5-c00c-4fab-9998-e5d4e285de48"},{"due":"Mar 6, 2020 8:30:00 AM","occurrence":"Mar 6, 2020 8:34:30 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"64ddcad5-f5c6-4970-bc0e-82328f734382"},{"due":"Mar 6, 2020 8:43:00 AM","occurrence":"Mar 6, 2020 8:43:07 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"e0ed489a-2a09-4a2c-89d3-51a1fa8aeece"},{"due":"Mar 6, 2020 10:30:00 PM","occurrence":"Mar 6, 2020 10:34:30 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"7eb67afe-369a-47eb-b7a3-2a9cb4e0e554"},{"due":"Mar 7, 2020 8:30:00 AM","occurrence":"Mar 7, 2020 8:34:35 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"c2f8eee7-cc16-4d53-9966-532125ce4733"},{"due":"Mar 7, 2020 8:43:00 AM","occurrence":"Mar 7, 2020 8:43:05 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"3a3eace8-e6e5-4d6b-98cc-b944b12ff246"},{"due":"Mar 7, 2020 10:30:00 PM","occurrence":"Mar 8, 2020 8:35:37 AM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"742e1b0a-d48f-4f81-8615-eab3f0022522"},{"due":"Mar 8, 2020 8:30:00 AM","occurrence":"Mar 8, 2020 8:35:42 AM","schedule_uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614","uid":"b7e5ba13-f2e2-47aa-b3e4-6c5c1ef9c433"},{"due":"Mar 8, 2020 8:43:00 AM","occurrence":"Mar 8, 2020 9:44:33 AM","schedule_uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66","uid":"62416198-efb7-4e72-9ed9-3096273307bd"},{"due":"Mar 8, 2020 10:30:00 PM","occurrence":"Mar 8, 2020 11:08:44 PM","schedule_uid":"fee048f9-9823-4430-a3f3-db91ef4d196a","uid":"ec22e9d1-d72a-4994-b54a-3bbf817bf085"}],"medications":[{"color_id":15,"deleted":false,"has_dpd_object":false,"icon_id":0,"photo_icon":false,"schedule_uids":["4c2699b9-9faa-4dd1-b977-4cf56ea26d66","26a6817a-cc25-438b-b357-7ea3e4ef2614","fee048f9-9823-4430-a3f3-db91ef4d196a","61657e3a-7805-4496-bb5f-3de45db6d9f3","2a599d45-7bcf-419a-933c-250ec6d64a83"],"uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0"}],"mood_logs":[{"date":"Mar 3, 2020 12:00:00 AM","rating":3,"uid":"9d4347bc-31d0-489d-b525-b7e5f3afdb9c"},{"date":"Mar 4, 2020 12:00:00 AM","rating":3,"uid":"d0c33b67-93c3-4928-afe1-3188d4acbb28"}],"pin":"777777","questions":[{"correctAnswer":2,"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":103,"uid":"1ada55cd-9530-4b90-9936-d3928ca51e56"},{"correctAnswer":1,"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":104,"uid":"bc06e4f4-256a-43ac-81fe-cfc7f1f2cf1e"},{"correctAnswer":0,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":203,"uid":"7fcc69ec-03fb-4913-99ef-02b639808c1b"},{"correctAnswer":2,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":206,"uid":"44eb8137-eed9-47d3-a253-b2b55e86f655"},{"correctAnswer":2,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":204,"uid":"f865285a-54b2-45d2-ae22-09afe01e6639"},{"correctAnswer":0,"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":102,"uid":"b5f48dcd-e686-400e-a328-53b87ee93324"},{"correctAnswer":2,"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":101,"uid":"9901be58-6e6e-4a8b-b6f7-54d1cb250301"},{"correctAnswer":3,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":202,"uid":"3013be0f-2d84-4c1b-b8bf-5903abc54578"},{"correctAnswer":3,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":201,"uid":"fc3d024d-266e-4ab9-9d1a-2a8b4bf85a82"},{"correctAnswer":3,"quiz_uid":"1331e483-187f-4b18-9fbc-03d2a7178c38","template_id":205,"uid":"b58b1acb-2412-414b-8577-f277d460306b"}],"quizzes":[{"date":"Mar 4, 2020 2:53:18 PM","question_uids":["1ada55cd-9530-4b90-9936-d3928ca51e56","bc06e4f4-256a-43ac-81fe-cfc7f1f2cf1e","7fcc69ec-03fb-4913-99ef-02b639808c1b","44eb8137-eed9-47d3-a253-b2b55e86f655","f865285a-54b2-45d2-ae22-09afe01e6639","b5f48dcd-e686-400e-a328-53b87ee93324","9901be58-6e6e-4a8b-b6f7-54d1cb250301","3013be0f-2d84-4c1b-b8bf-5903abc54578","fc3d024d-266e-4ab9-9d1a-2a8b4bf85a82","b58b1acb-2412-414b-8577-f277d460306b"],"uid":"1331e483-187f-4b18-9fbc-03d2a7178c38"}],"schedules":[{"deleted":true,"deletedDate":"Mar 2, 2020 12:00:00 AM","log_uids":["c620ea3c-1014-4bbe-adbb-87abad8c837a","cc744ff0-330f-45f1-86ab-fdfe199475e7","00a0e8f0-f422-46a6-a25e-630b8930c900","e0ed489a-2a09-4a2c-89d3-51a1fa8aeece","3a3eace8-e6e5-4d6b-98cc-b944b12ff246","62416198-efb7-4e72-9ed9-3096273307bd"],"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","repetitionCount":1,"repetitionUnit":2,"startDate":"Mar 3, 2020 8:43:00 AM","uid":"4c2699b9-9faa-4dd1-b977-4cf56ea26d66"},{"deleted":true,"deletedDate":"Mar 8, 2020 12:00:00 AM","log_uids":["ed5a8a0b-bf12-4135-affa-ccaa5658db3a","e38fa4e0-f6f3-4755-a475-62598e1b76d8","f56ad0cb-ee3d-4ee2-8e97-f189f73b1158","64ddcad5-f5c6-4970-bc0e-82328f734382","c2f8eee7-cc16-4d53-9966-532125ce4733","b7e5ba13-f2e2-47aa-b3e4-6c5c1ef9c433"],"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","repetitionCount":1,"repetitionUnit":2,"startDate":"Mar 3, 2020 8:30:00 AM","uid":"26a6817a-cc25-438b-b357-7ea3e4ef2614"},{"deleted":true,"deletedDate":"Mar 8, 2020 12:00:00 AM","log_uids":["c0fa0e71-c7c9-46ed-9089-aeab14099260","6ca084ef-326e-4832-9bba-f12e4e70b78b","8020f051-5fcd-4ded-9570-05b5c6cf7b52","d49f41f5-c00c-4fab-9998-e5d4e285de48","7eb67afe-369a-47eb-b7a3-2a9cb4e0e554","742e1b0a-d48f-4f81-8615-eab3f0022522","ec22e9d1-d72a-4994-b54a-3bbf817bf085"],"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","repetitionCount":1,"repetitionUnit":2,"startDate":"Mar 2, 2020 10:30:00 PM","uid":"fee048f9-9823-4430-a3f3-db91ef4d196a"},{"deleted":false,"log_uids":[],"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","repetitionCount":1,"repetitionUnit":2,"startDate":"Mar 9, 2020 8:30:00 AM","uid":"61657e3a-7805-4496-bb5f-3de45db6d9f3"},{"deleted":false,"log_uids":[],"medication_uid":"7d7748fc-eef2-4fc7-a0e4-6760a7f4a0c0","repetitionCount":1,"repetitionUnit":2,"startDate":"Mar 9, 2020 10:30:00 PM","uid":"2a599d45-7bcf-419a-933c-250ec6d64a83"}],"visit_logs":[{"date":"Mar 3, 2020 10:34:56 PM","page":"StatisticsFragment","uid":"2ea061b7-4d48-4e6f-ba15-b80ccbdb2ae7"},{"date":"Mar 4, 2020 2:53:19 PM","page":"StatisticsFragment","uid":"d6f53a6b-d487-4d0b-8bb1-0acd0b7490e7"},{"date":"Mar 4, 2020 2:53:30 PM","page":"StatisticsFragment","uid":"1d38cc1e-bef6-46f4-a583-a99c5c7cc32f"},{"date":"Mar 4, 2020 2:53:33 PM","page":"StatisticsFragment","uid":"0fbfc340-2fdb-4fac-85e7-7fde7530568e"},{"date":"Mar 4, 2020 8:10:48 PM","page":"QuizActivity","uid":"1aa8160c-83ce-4c9b-85b4-5f7b0609b07d"},{"date":"Mar 7, 2020 10:34:52 PM","page":"StatisticsFragment","uid":"9b4db507-cfa7-4fbb-90e2-202ec187e5cf"},{"date":"Mar 7, 2020 10:35:00 PM","page":"StatisticsFragment","uid":"b582607a-52b3-4cc4-8790-a10dee974c17"}]}
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
	currentValue = {
		correctAnswer: currentValue.correctAnswer === undefined ? "" : currentValue.correctAnswer,
		medication_uid: currentValue.medication_uid || "",
		quiz_uid: currentValue.quiz_uid || "",
		template_id: currentValue.template_id,
		uid: currentValue.uid || "",
		userAnswer: currentValue.userAnswer === undefined ? "" : currentValue.userAnswer
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
