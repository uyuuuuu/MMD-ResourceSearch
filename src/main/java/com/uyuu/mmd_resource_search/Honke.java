package com.uyuu.mmd_resource_search;

//////////////////////////
//////本家動画クラス  ////////
//////////////////////////

public class Honke {
	private String title;
	private String channel;
	private String description;
	public Honke(String title, String channel) {
		this.title = title;
		this.channel = channel;
	}
	// get
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getChannel() {
		return channel;
	}
	
	/////////////////////////
	//// 検索ワードを整理する
	/////////////////////////
	public String getEditTitle() {
		String q = this.title;
		// 【】などを削除
		q = q.replaceAll("【.*?】", "");
		q = q.replaceAll("\\[.*?\\]", "");
		q = q.replaceAll("（.*?）", "");
		q = q.replaceAll("\\(.*?\\)", "");
		q = q.replaceAll("(feat|by).? ?\\S*[-/]?", ""); //[featかby].?[半角スペ?][空白以外][-/があるかも]
		// 文字を消す
		q = q.replaceAll("([cC]overd?|[歌踊]ってみた|)", "");
		q = q.replaceAll("[をに]?(歌わ|踊ら)せてみた","");
		q = q.replaceAll("オリジナル(ソング)?","");
		// 投稿者名と同じとこを消す
		q = q.replace(editChannel(), "");
		// よく出るボカロ名を消す
		q = q.replaceAll("(初音ミク|[hH]atsune[mM]iku|鏡音リン|鏡音レン|巡音ルカ|可不|[kK]afu|星界|GUMI|flower|(音街)?ウナ|(重音)?テト)(さん)?に?", "");
		//記号を削除
		q = q.replaceAll("[^\\wぁ-んァ-ヶー一-龯&＆ 　]+", "");
		q = q.replaceAll("/|-|~|_|\\.|,", "");
		//先頭末尾の空白削除
		q = q.strip();
		System.out.println("検索ワード。。。"+q);
		return q;
	}
	
	/////////////////////////
	//// チャンネル名を整理する
	/////////////////////////
	public String editChannel() {
		//channelの /以降 と (),（）の中身 を削除
		String edit = channel;
		edit = edit.replaceAll("/.*?", "");
		edit = edit.replaceAll("（.*?）", "");
		edit = edit.replaceAll("\\(.*?\\)", "");
        return edit;
	}
	


}
