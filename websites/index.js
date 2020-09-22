let parseCookie = function(property) {
    return RegExp("(?<=" + property + "=).*?(?=;)").exec(document.cookie)[0];
}

// console.log(document.cookie === "" || parseCookie("username") === "null");
console.log(document.cookie);
console.log(document.cookie === "");
console.log(document.cookie === "" || parseCookie("username") === "null");


if (document.cookie === "" || parseCookie("username") === "null") {
    console.log("in the loop");
    window.onload = function() {
        let joinButton = document.getElementById("joinbutton");
        let userNameNode = document.getElementById("username");
        let roomNameNode = document.getElementById("roomname");
        let joinRoom = function () {
            if (userNameNode.value.length == 0) {
                alert("Username can't be null");
            } else if (roomNameNode.value.length == 0) {
                alert("Roomname can't be null");
            } else {
                localStorage.setItem("username", userNameNode.value);
                localStorage.setItem("roomname", roomNameNode.value);
                window.location.assign("chatroom.html");
            }
        }
        joinButton.onclick = joinRoom;
        document.onkeypress = function (ev) {
            if (ev.keyCode == 13) {
                joinRoom();
            }
        }
    }

} else {
    window.location.assign("chatroom.html");
}