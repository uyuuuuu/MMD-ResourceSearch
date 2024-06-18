package com.uyuu.mmd_resource_search;

///////////////////////////////////
/////////  メインの実行クラス  //////////
///////////////////////////////////
//https://www.youtube.com/watch?v=eSW2LVbPThw

public class MMDSearch {
	public static void main(String[] args) {
		YouTubeAPI youtube = new YouTubeAPI("youtube.properties", 10);
        NicoAPI nico = new NicoAPI(10, "MMDモーション配布あり", "MMDカメラ配布あり");
        Tool tool = new Tool(youtube,nico);

    // MMD動画検索
        mmdSearch(youtube,nico,tool);
	}
	
	public static void mmdSearch(YouTubeAPI youtube, NicoAPI nico, Tool tool) {		
	// 入力処理
		Input in = new Input(youtube, nico, tool);     
	// 本家探し
		String[] origID_yn = tool.pickOrigID(in.getDescription());
	// 検索楽曲の絞り込み
		String title,channel;
		if(origID_yn!=null) {
			String[] origDetails = tool.searchDetail(origID_yn[0], origID_yn[1]);
			title = origDetails[0];
			channel = origDetails[2];
		}else {
			title = in.getTitle();
			channel = in.getChannel();
		}
		Honke honke = new Honke(title,channel);
		
		//検索、出力を実行
		tool.search(honke.getEditTitle(),title);	
		in.reInput();
	}
}

