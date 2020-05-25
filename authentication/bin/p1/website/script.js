
function get_user_info(){
    var request = new XMLHttpRequest();

    request.onreadystatechange = function(){	
		if	(this.readyState === 4 && this.status === 200){	
            console.log("###############################");
            parse_and_apply(this.response);
		}	
    };
    request.withCredentials = true;
	request.open("GET", "/profile_info", true);	
	request.send();
}//profile_info

function parse_and_apply(ress){
    console.log(ress);
    var user_info = ress.split(",");
    var v1 = user_info[0];
    v1 = unEntity(v1);
    var v2 = user_info[1];
    v2 = unEntity(v2);
    document.getElementById("email").innerText = v1;
    document.getElementById("favClass").innerText = v2;
}

function unEntity(str){
  return str.replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">");
}