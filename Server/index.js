const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
const googleMapsClient = require('@google/maps').createClient({
  key: 'AIzaSyC8FwfQCNqK9DvS67nn1wpliM7aVl1Z3sQ'
});
admin.initializeApp(functions.config().firebase);
const maxSrcDistance = 1000;
const maxDestDistance = 5000;
const maxNumOfPassengers =4;

exports.dealwithRequests = functions.database.ref('/requests/{pushId}')
    .onCreate(event => {
      const requestId = event.data.key;
      const request=event.data.val();
      const requestRef=event.data.ref;
      const requesterId=request["requesterId"];
	  return deletePreviousRequests(requestId, requesterId).then(()=>{
	  		return admin.database().ref('/users').child(requesterId).child('rating').once('value');
	  }).then(snap=>{
      return foo(request, requestRef,snap.val())
    });

    });

function foo(request, requestRef, rating){
  const groupsRef= admin.database().ref('/groups');
	const groupsQuery= groupsRef.orderByChild('closed').equalTo(false);
	 return groupsQuery.once('value').then(snap => {
			  const groups=snap.val();
			  const possibleGroups=[];
			  for(var groupId in groups){
			  	if(groups.hasOwnProperty(groupId)){
			  		var meetingPoint=groups[groupId]["meetingPoint"];
			  		var requestSrc=request["src"];
			  		var srcDistance= getDistance(str2latlng(meetingPoint), str2latlng(requestSrc));
			  		var avgDest=groups[groupId]["avgDest"];
				  	var requestDest=request["dest"];
            var totalNumOfPassengers=request["numOfPassengers"]+groups[groupId]["numOfPassengers"];
				  	var destDistance= getDistance(str2latlng(avgDest), str2latlng(requestDest));
			  		if(srcDistance<maxSrcDistance && destDistance<maxDestDistance && totalNumOfPassengers<maxNumOfPassengers){
			  			possibleGroups.push(groupId);
			  		}
			  	}
			  }
			  if(possibleGroups.length==0){
			  	//Create new group:
			  	const newGroupId=groupsRef.push().key;
			  	return groupsRef.child(newGroupId).set({"meetingPoint":request["src"], "avgDest":request["dest"], "numOfUsers":1, "numOfPassengers":request["numOfPassengers"], closed:false,active:false, avgRating:rating}).then(()=>{
			  		return requestRef.child("groupId").set(newGroupId);
			  	});
			  }else{
          possibleGroups.sort(function(a,b){
            return Math.abs(rating-a["avgRating"])-Math.abs(rating-b["avgRating"]);
          });
          const matchGroupId=possibleGroups[0];
          const matchGroup=groups[matchGroupId];
          const numOfUsers=matchGroup["numOfUsers"];
          const numOfPassengers=matchGroup["numOfPassengers"];
          const newMeetingPoint=avgLatlng(matchGroup["meetingPoint"],numOfUsers,request["src"]);
          const newAvgDest=avgLatlng(matchGroup["avgDest"],numOfUsers,request["dest"]);
          const newNumOfPassengers =numOfPassengers+request["numOfPassengers"];
          const oldAvgRating= matchGroup["avgRating"];
          const newAvgRating=(numOfUsers*oldAvgRating+rating)/(numOfUsers+1);
          const matchGroupRef=groupsRef.child(matchGroupId);
          var promises=[]
          promises.push(matchGroupRef.child("meetingPoint").set(newMeetingPoint));
          promises.push(matchGroupRef.child("avgDest").set(newAvgDest));
          promises.push(matchGroupRef.child("numOfUsers").set(numOfUsers+1));
          promises.push(matchGroupRef.child("numOfPassengers").set(newNumOfPassengers));
          promises.push(matchGroupRef.child("active").set(true));
          promises.push(matchGroupRef.child("avgRating").set(newAvgRating));
          if(newNumOfPassengers>=maxNumOfPassengers)
              promises.push(matchGroupRef.child("closed").set(true));
          return Promise.all(promises).then(()=>{
            return requestRef.child("groupId").set(matchGroupId);
          });
        }
		  });
}


exports.sendPush = functions.database.ref('/messages/{groupId}/{messageId}/authorId')
    .onCreate(event => {
      const groupId = event.params.groupId;
      const sender=event.data.val();
        const requestsRef=admin.database().ref('/requests');
        const groupReqs=requestsRef.orderByChild('groupId').equalTo(groupId);
        return groupReqs.once('value').then(snap=>{
          const promises=[];
          const requests=snap.val();
          for(var requestId in requests){
            if(requests.hasOwnProperty(requestId)){
              const requesterId=requests[requestId]['requesterId'];
              console.log("RequesterId: "+requesterId);
              promises.push(admin.database().ref('/users').child(requesterId).child('messageToken').once('value'));
            }
          }
          return Promise.all(promises);
        }).then(snaps =>{
          const tokens=[];
		  payload={
			data: {
				senderId: sender
			}
		  };
          for(var i=0;i<snaps.length; i++){
            tokens.push((snaps[i]).val());
          }
           return admin.messaging().sendToDevice(tokens, payload);
		
});
	});


function addRequestToGroup(requestId, groupId){
	const groupRef=admin.database().ref('/requests/'+requestId+'/groupId');
	return groupRef.set(groupId);
}

//delete previous requests from the same user
function deletePreviousRequests(requestId, requesterId){
	const reqsRef=admin.database().ref('/requests');
	const previousReqs=reqsRef.orderByChild('requesterId').equalTo(requesterId);
	return previousReqs.once('value').then(snap=>{
		const reqs=snap.val();
		const promises=[];
		for(request in reqs){
			if(reqs.hasOwnProperty(request)){
				if(request!=requestId){
					promises.push(reqsRef.child(request).set(null));
				}
			}
		}
		return Promise.all(promises);
	});

}

var rad = function(x) {
  return x * Math.PI / 180;
};

var getDistance = function(p1, p2) {
  var R = 6378137; // Earth’s mean radius in meter
  var dLat = rad(p2.lat - p1.lat);
  var dLong = rad(p2.lng - p1.lng);
  var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(rad(p1.lat)) * Math.cos(rad(p2.lat)) *
    Math.sin(dLong / 2) * Math.sin(dLong / 2);
  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  var d = R * c;
  return d; // returns the distance in meter
};

var str2latlng = function(str){
	var arr=str.split(',');
	return {lat:parseFloat(arr[0]), lng:parseFloat(arr[1])}
}


function avgLatlng(avg, num, latlng){
	var avg=str2latlng(avg);
	var latlng=str2latlng(latlng);
	var newlat=(avg.lat*num+latlng.lat)/(num+1);
	var newlng=(avg.lng*num+latlng.lng)/(num+1);
	return newlat+","+newlng;
}
