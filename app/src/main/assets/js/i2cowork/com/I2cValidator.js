/************************************************
 * 파일 처리.
 ************************************************/

com.i2cowork.Validator = function() {

	this.formValidate = function(formObj, callback) {
		var validEl = $(formObj).find('input[valid-rule], textarea[valid-rule], select[valid-rule]');
		var returnFlg = true;
		$.each(validEl, function(k, el){
			var type = el.type;
			console.log(type);
			var maxlength = $(el).attr("maxlength");
			var minlength = $(el).attr("minlength");

			var title = $(el).attr("valid-title");
			var rule = $(el).attr("valid-rule");
			var validRule = eval(rule);
			console.log(el.name + " : " + rule);			

			$.each(validRule, function(index, rule) {
				// 필수 체크
				if (rule == 'required') {
					switch (type) {
					case "text":
						if (el.value == "") {
							alert(title+" 항목은 필수 입력입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
						break;
					case "textarea":
						if (el.value == "") {
							alert(title+" 항목은 필수 입력입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
						break;
					case "hidden":
						if (el.value == "") {
							alert(title+" 항목은 필수 입력입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
						break;
					case "radio": 
						var name = el.name;
						if (!$("input:radio[name="+name+"]").is(":checked")) {
							alert(title+" 항목은 필수 선택입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
						break;
					}
					if(type == "select-one") {
						if (el.value == "") {
							alert(title+" 항목은 필수 입력입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
					} else if(type == "select-multiple") {
						if (el.value == "") {
							alert(title+" 항목은 필수 입력입니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
					}
				}
				if (el.value != null && el.value != "") {
					// 전화번호 형식
					if (rule == 'phone' ) {
							var regExp = /^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})-?[0-9]{3,4}-?[0-9]{4}$/;
							if (!regExp.test(el.value)) {
								alert(title+" 항목은 잘못된 전화번호입니다. 예) 02-XXXX-XXXX 또는 02XXXXXXX");
								$(el).focus();
								returnFlg = false;
								return false;
							}
					}
					//전화번호 앞자리
					if (rule == 'phoneHead' ) {
							var regExp = /^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})$/;
							if (!regExp.test(el.value)) {
								alert(title+" 항목은 잘못된 전화번호입니다. 예) 02-XXXX-XXXX 또는 02XXXXXXX");
								$(el).focus();
								returnFlg = false;
								return false;
							}
					}
					//전화번호 중간
					if (rule == 'phoneBody' ) {
							var regExp = /^[0-9]{3,4}$/;
							if (!regExp.test(el.value)) {
								alert(title+" 항목은 잘못된 전화번호입니다. 예) 02-XXXX-XXXX 또는 02XXXXXXX");
								$(el).focus();
								returnFlg = false;
								return false;
							}
					}
					//전화번호 뒷자리
					if (rule == 'phoneEnd' ) {
							var regExp = /^[0-9]{4}$/;
							if (!regExp.test(el.value)) {
								alert(title+" 항목은 잘못된 전화번호입니다. 예) 02-XXXX-XXXX 또는 02XXXXXXX");
								$(el).focus();
								returnFlg = false;
								return false;
							}
					}
					// email 형식
					if (rule == 'email' ) {
						var regExp = /[0-9a-zA-Z][_0-9a-zA-Z-]*@[_0-9a-zA-Z-]+(\.[_0-9a-zA-Z-]+){1,2}$/;
						 if (!regExp.test(el.value)) {
					        alert(title+" 항목은 잘못된 이메일 형식입니다. 예) email@XXX.XXX");
					        $(el).focus();
							returnFlg = false;
					        return false;
					    }
					}
					// 숫자만 입력 가능
					if (rule == 'number' ) {
						var regExp = /^[0-9]*$/;
						if (!regExp.test(el.value)) {
							alert(title+" 항목은 숫자만 입력 가능합니다.");
							$(el).focus();
							returnFlg = false;
							return false;
						}
					}
				}
            });

			if (!returnFlg) return false;
		});
		callback(returnFlg);
	};
};

var $Validator = new com.i2cowork.Validator();				// 파일 처리.