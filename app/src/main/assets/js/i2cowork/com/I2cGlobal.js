
/**
 * 전역변수
 **/

/**
 * 글로벌 스크립트 객체 변수 선언
 */
var com = com || {};
com.i2cowork = {};
// Core 변수.
com.i2cowork.Global		= function(){};		// 전역변수
com.i2cowork.Util		= function(){};		// 유틸
com.i2cowork.Network	= function(){};		// AJAX 네트워크 처리
com.i2cowork.Event		= function(){};		// 이벤트 바인딩.
com.i2cowork.Page		= function(){};		// 페이지 처리 공통
com.i2cowork.Popup		= function(){};		// 팝업

// 업무용
com.i2cowork.Validator	= function(){};		// 폼 Validator
com.i2cowork.Calendar	= function(){};		// 달력 콤포넌트
com.i2cowork.File		= function(){};		// 파일 관련
com.i2cowork.UserInfo	= function(){};		// 사용자 객체
com.i2cowork.DeviceInfo	= function(){};		// 장치 객체
com.i2cowork.SnsPost    = function(){};		// SNS 공통
com.i2cowork.Message    = function(){};		// 메세지 보내기.

// 전역 변수
com.i2cowork.Global = function() {

	// 통신 처리 데이터 타입.
	this.DataType = {
		JSON : "json", 
		HTML : "html", 
		XML:"xml"
	};
	
	// 화면 레이어 변수.
	this.Layer = {
		Header   : "#Header", 
		Menu     : "#Menu",
		Profile  : "#Profile",
		Contents : "#Contents",
		Follow   : "#Follow",
	};
	this.NET_PRIFIX = "/i2cowork";
};
var $Global = new com.i2cowork.Global();

// 사용자 변수
var $UserInfo = com.i2cowork.UserInfo = {};
var $DeviceInfo = com.i2cowork.DeviceInfo = {};
