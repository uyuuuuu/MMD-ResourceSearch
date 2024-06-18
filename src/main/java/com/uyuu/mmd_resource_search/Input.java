package com.uyuu.mmd_resource_search;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//////////////////////////
//////入力関連クラス  ////////
//////////////////////////

public class Input {
	private static Scanner scanner;
	private YouTubeAPI youtube;
	private NicoAPI nico;
	private Tool tool;
	private String in_url;
	private String in_yn;
	private String in_id;
	private String in_title;
	private String in_description;
	private String in_channel;
	// コンストラクタ
	public Input(YouTubeAPI youtube, NicoAPI nico, Tool tool) {
		this.youtube = youtube;
		this.nico = nico;
		this.tool = tool;
		scanner = new Scanner(System.in);
		inputURL(scanner); // url,yn
		this.in_id = pickID(); //id
		setDetail(); //情報をサーチしてセット //title,description,channel
	}
	// get
	public String getTitle() {
		return in_title;
	}
	public String getDescription() {
		return in_description;
	}
	public String getChannel() {
		return in_channel;
	}

	// URL入力 & yn判定
	private void inputURL(Scanner scanner) {
		System.out.println("MMD動画とモーションを探しましょう！");
		System.out.println("youtubeまたはniconicoにある動画のURLを入力してください！");
		String input = scanner.next();
		String url = input;
		if(checkYN(url)!=null) {
			this.in_url = input;
			this.in_yn = checkYN(in_url);
		}else {
			inputURL(scanner);
		}
	}

	// yn判定
	private String checkYN(String url) {
		String yn = null;
		Pattern y_p1 = Pattern.compile("://www.youtube.com/watch\\?v=([\\w-]{11})");
		Pattern y_p2 = Pattern.compile("://youtu.be/([\\w-]{11})");
		Pattern n_p1 = Pattern.compile("://www.nicovideo.jp/watch/sm(\\d{6,9})");
		Pattern n_p2 = Pattern.compile("sm(\\d{6,9})");
		Matcher url_m1 = y_p1.matcher(url);
		Matcher url_m2 = y_p2.matcher(url);
		Matcher url_m3 = n_p1.matcher(url);
		Matcher url_m4 = n_p2.matcher(url);
		if (url_m1.find() || url_m2.find()) {
			yn = "y";
		} else if (url_m3.find() || url_m4.find()) {
			yn = "n";
		} else {
			System.out.println("Error!!URLが間違っている可能性があります。\n");
			yn = null;
		}
		return yn;
	}
	
	// 入力URLからIDを抽出
	private String pickID() {
		String id =null;
		if(in_yn=="y") id = youtube.getVideoID(in_url);
		else if(in_yn=="n") id = nico.getNicoID(in_url);
		return id;
	}
	
	// 入力URLの情報を取得してセット
	private void setDetail() {
		String[] detail = null;
		detail = tool.searchDetail(in_id,in_yn);
		//セット
		this.in_title = detail[0];
		this.in_description = detail[1];
		this.in_channel = detail[2];
	}
	
	// 再入力
	public void reInput() {
        System.out.println("新しいURLで探したい場合は'u'を、");
		System.out.println("検索ワードを変えたい場合は'w'を、");
		System.out.println("終了したい場合はそれ以外を入力してください。");
		String command = scanner.next();
		if(command.equals("w")) {
			System.out.println("検索ワードを入力してください");
			String re_word = scanner.next();
			tool.search(re_word,in_title);
		}else if(command.equals("u")) {
			MMDSearch.mmdSearch(youtube,nico,tool);
		}else {
			System.out.println("終了");
		}
		scanner.close();
	}
	
}
