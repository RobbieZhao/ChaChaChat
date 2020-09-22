
let clearCookie = function() {
    document.cookie = "expires=Thu, 01 Jan 1970 00:00:01 GMT";
}

let setCookie = function(userName, roomName, minutesToExpire) {
    document.cookie = "username=" + userName;
    document.cookie = "roomname=" + roomName;

    let date = new Date();
    date.setTime(date.getTime() + (minutesToExpire * 60 * 1000));
    let expires = "expires="+date.toGMTString() + ";";
    document.cookie = expires;
}

let addMessageToMessageWindow = function(userName, message) {
    let messageWindow = document.getElementById("messagewindow");

    let outterDiv = document.createElement("div");
    outterDiv.setAttribute("class", "messagebox");

    let innerDiv = document.createElement("div");
    innerDiv.innerHTML = "<p>" + message + "</p>";

    let label = document.createElement("label");
    if (userName === UserName) {
        label.setAttribute("class", "right");
        innerDiv.setAttribute("class", "mymessage message");
    } else {
        label.setAttribute("class", "left");
        innerDiv.setAttribute("class", "othersmessage message");
    }
    label.innerHTML = userName;
    let br = document.createElement("br");

    outterDiv.append(label, br, innerDiv);

    // messageWindow.appendChild(outterDiv);
    messageWindow.prepend(outterDiv);
}

let handleMessage = function(event) {
    let obj = JSON.parse(event.data);
    addMessageToMessageWindow(obj.user, obj.message);
}


let mySocket = new WebSocket("ws://" + location.host);


mySocket.onopen = function() {
    UserName = localStorage.getItem("username");
    let RoomName = localStorage.getItem("roomname");

    // setCookie(UserName, RoomName, 30);

    mySocket.send("join " + RoomName);
};

mySocket.onmessage = handleMessage;

window.onload = function() {
    let sendMessage = function() {
        let messageNode = document.getElementById("input");
        if (messageNode.value != "") {
            mySocket.send(UserName + " " + messageNode.value);
            messageNode.value = "";
        }
    }
    document.onkeypress = function (ev) {
        if (ev.keyCode == 13) {
            sendMessage();
        }
    }
}
