

/**
 * i2cMessage.js
 * 메세지 처리
 * @author i2max-dev
 * @since 2015.10.23
 * Copyright (c) 2003-2015 i2max
 **/

var com = com || {};
com.i2cowork = com.i2cowork || {};
com.i2cowork.Message = function() {
	var _this = this;

	// 메세지 보내기 창 활성
	this.showSendBox = function(tar_usr_id){
		var layerContext = ''
			+ '<div class="layer_wrap layer_message_send" id="layer_message_send">'
		 	+ '<div class="layer_area" tabindex="0" data-width="">'
		 	+ '    <div class="layer_tit">'
			+ '	       <p>메세지 보내기</p>'
			+ '    </div>'
			+ '    <div class="layer_con">'
			+ '        <div class="message_send">'
			+ '            <input type="hidden" name="dm_tar_usr_id" value="'+tar_usr_id+'"/>' 
			+ '            <textarea placeholder="내용을 입력해 주세요." name="dm_message" id="dm_message"></textarea>'
			+ '        </div>'
			+ '    </div>'
			+ '    <div class="layer_btn">'
			+ '        <a class="type_navy btn_message_send" href="#" onclick="$Message.sendMessageBySendBox();">메시지보내기</a>'
			+ '    </div>'
			+ '    <a class="btn_layer_close" href="javascipt:;" onclick="$(this).parent().parent().remove();">'
			+ '        <img src="/images/i2cowork/common/btn_layer_close.png" alt="팝업 닫기" /></a>'
			+ '</div>'
			+ '<div class="layer_bg"></div>'
		    + '</div>';
		$.when($("body").append(layerContext)).done(function() {
			//$(".c_d2_pop", mainContext).css({'display':'block'});
			$("#layer_message_send").show();
			$Event.gb_layPopupHeight2();
		});
	};

	// 메시지 함.
	this.showMessageBox = function() {
		$Page.CallAPI({
			url : "/sns/message/i2cMessageBox.do",
			targetSelector : "body",
			htmlMode : "append"
		}, function() {
			$(".layer_message").show();
		});
	};

	this.sendMessageBySendBox = function() {
		var target_type = "user";
		var tar_usr_id = $("#dm_tar_usr_id").val();
		var dm_message = $("#dm_message").val();
		_this.sendMessage({
			target_type : target_type,
			usr_id : tar_usr_id,
			dm_message : dm_message,
		});
	};

	this.sendMessage = function(custData) {
		var options = {
			url : "/sns/push/sendSnsMessageBox.json",
			dataType : $Global.DataType.JSON,
			custData : custData,
			systemAlert : false
		};
		$Page.CallAPI(options);
	};
};

var $Message = new com.i2cowork.Message();				// 팝업.
