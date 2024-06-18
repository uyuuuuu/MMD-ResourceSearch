package com.uyuu.mmd_resource_search;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

///////////////////////////////////
/////////  共通機能クラス  ////////////
///////////////////////////////////


public class Tool {
	private YouTubeAPI youtube;
	private NicoAPI nico;
	public Tool(YouTubeAPI youtube, NicoAPI nico) {
		this.youtube = youtube;
		this.nico = nico;
	}
	////////////////////////////
	/// 概要欄 > 本家[ID,yn]
	////////////////////////////
	public String[] pickOrigID(String description) {
		String y_st1 = "https?://www.youtube.com/watch\\?v=([\\w-]{11})";
		String y_st2 = "https?://youtu.be/([\\w-]{11})";
		String n_st1 = "https?://www.nicovideo.jp/watch/sm(\\d{6,9})";
		String n_st2 = "sm(\\d{6,9})"; //運良く引っかかるのを避けるため先に↑で探す
		//本家の文字があるならそこを採用
		Pattern y1_honke = Pattern.compile("本家[\\s\\S]*"+y_st1); //
		Pattern y2_honke = Pattern.compile("本家[\\s\\S]*"+y_st2); //
		Pattern n_honke = Pattern.compile("本家[\\s\\S]*"+n_st2); //sm12345
		//なければ先頭のurlを採用
		Pattern y_p1 = Pattern.compile(y_st1);
		Pattern y_p2 = Pattern.compile(y_st2);
		Pattern n_p1 = Pattern.compile(n_st1);
		Pattern n_p2 = Pattern.compile(n_st2);
		// 新作情報は除く
		Matcher honkeY_m1 = y1_honke.matcher(description);
		Matcher honkeY_m2 = y2_honke.matcher(description);
		Matcher honkeN_m = n_honke.matcher(description);
		// 本家の文字がなければ本家じゃなさそうなURLを消してからmatch
		description = description.replaceAll("(新作|新曲|次|前)(.*)\n?(.*)("+ y_p1 + "|" + y_p2 +"|"+n_p2+")", "");
		Matcher url_m1 = y_p1.matcher(description);
		Matcher url_m2 = y_p2.matcher(description);
		Matcher url_m3 = n_p1.matcher(description);
		Matcher url_m4 = n_p2.matcher(description);
    	String matchID = null;
    	String yn = null;
    	
    	if(honkeY_m1.find()) {
    		matchID = honkeY_m1.group(1);
    		yn="y";
    	}else if(honkeY_m2.find()) {
    		matchID = honkeY_m2.group(1);
    		yn="y";
    	}else if(honkeN_m.find()) {
    		matchID = honkeN_m.group(1);
    		yn="n";
    	}else if(url_m1.find()) {
    		matchID = url_m1.group(1);
    		yn="y";
    	}else if(url_m2.find()) {
    		matchID = url_m2.group(1);
    		yn="y";
    	}else if(url_m3.find()) {
    		matchID = url_m3.group(1);
    		yn="n";
    	}else if(url_m4.find()) {
    		matchID = url_m4.group(1);
    		yn="n";
    	}else{ // inが本家かも？
    		System.out.println("オリジナル動画、または本家URLのないカバー動画の可能性があります。");
    		return null;
    	}
		String[] result = { matchID, yn };
		return result;
	}

	public String[] searchDetail(String id,String yn) {
		String[] detail = null;
		if(id!=null) {
			if(yn=="y") detail = youtube.getDetail(id);
			else if(yn=="n") detail = nico.getDetail(id);
		}
		return detail;
	}
	
	//////////////////////////
	//// 概要欄から素材を探す
	//////////////////////////
	public static void pickSozai(String description) {
		String sozai = "(D[lL]|[dD]ownload|配布|場所|モーション|[mM]otions?|カメラ|[cC]amera|ステージ|[sS]tage|モデル|[mM]odels?|MME|mme|エフェクト|[eE]ffects?)";
		Pattern p_bowl = Pattern.compile(sozai+"(?!.*様)[\\s\\S]*(https?://bowlroll.net/(file/|up/dl)\\d{4,6})");
		Pattern p_common = Pattern.compile(sozai+"(?!.*様)[\\s\\S]*(https?://commons.nicovideo.jp/works/sm(\\d{6,9}))");
		Pattern p_seiga = Pattern.compile(sozai+"(?!.*様)[\\s\\S]*(https?://seiga.nicovideo.jp/seiga/im(\\d{5,9}))");
		Pattern p_3d = Pattern.compile(sozai+"(?!.*様)[\\s\\S]*(https?://3d.nicovideo.jp/works/td(\\d{5,9}))");
		Pattern p_nc = Pattern.compile(sozai+"(?!.*様)[\\s\\S]*(https?://(nico.ms|www.nicovideo.jp/watch)/sm(\\d{5,9}))");
		Pattern p_name = Pattern.compile(sozai+"(.*様)+");
		Pattern p_pass = Pattern.compile("(パスワード|パス|[Pp][Ww]|pass|[Pp]ass[Ww]ord|[kK]ey)"+".*\n?");
		String desc_br = description.replaceAll("<br( /)?>", "\n");
		Matcher m_bowl = p_bowl.matcher(desc_br);
		Matcher m_common = p_common.matcher(description);
		Matcher m_seiga = p_seiga.matcher(description);
		Matcher m_3d = p_3d.matcher(description);
		Matcher m_nc = p_nc.matcher(description);
		Matcher m_name = p_name.matcher(desc_br);
		Matcher m_pass = p_pass.matcher(desc_br);
		// 見つかったぶん全て表示
		while(m_bowl.find()) {
			String dl = new Scrap(m_bowl.group(2)).getBowlDL();
			System.out.print(" ▶"+m_bowl.group(1)+": "+m_bowl.group(2));
			System.out.println("  ("+dl+")"); //DL数
		}
		while(m_common.find()) {
			System.out.println(" ▶"+m_common.group(1)+": "+m_common.group(2));
		}
		while(m_seiga.find()) {
			System.out.println(" ▶"+m_seiga.group(1)+": "+m_seiga.group(2));
		}
		while(m_3d.find()) {
			System.out.println(" ▶"+m_3d.group(1)+": "+m_3d.group(2));
		}
		if(m_pass.find()) { //素材DLパスワード
			System.out.println(" ▷" + m_pass.group().replaceAll("\n",""));
		}
		while(m_nc.find()) {
			System.out.println(" ▶"+m_nc.group(1)+": "+m_nc.group(2));
		}
		while(m_name.find()) {
			// タグ、記号、無駄な文字削除
			String name = m_name.group(2).replaceAll("<br>", "");
			name = name.replaceAll("(制作|作成)","");
			name = name.replaceAll("[^\\wぁ-んァ-ヶー一-龯/,、&\\| 　]", "");
			name = name.replaceAll(".*?、","");
			
			System.out.println(" ▶" + m_name.group(1)+": " + name);
		}
			
	}

	////////////////////
	//// 検索して出力
	////////////////////
	public void search(String query, String orig_title) {
		if(query!=null) {
			System.out.println("-------------------------------------------------------------");
			System.out.println("--------------------------- youtube -------------------------");
			System.out.println("-------------------------------------------------------------\n");
			youtube.searchVideo(query+" mmd", orig_title);
			
			System.out.println("-------------------------------------------------------------");
			System.out.println("-------------------------- niconico -------------------------");
			System.out.println("-------------------------------------------------------------");
			try {
				nico.wordSearch(query, orig_title);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
