var Signing = function(){
	var self = this;
	self.displaySigningCanvas = function (pdf, success, failure) {
	    cordova.exec(success, failure, "SnapcartSigning", "displaySign", [pdf]);
	}
}

if(!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.signing) {
    window.plugins.signing = new Signing();
}

if (typeof module !== "undefined" && module.exports) {
  module.exports = Signing;
}
