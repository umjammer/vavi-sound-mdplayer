# MDPlayer
VGMファイルなどのPlayer(メガドライブ音源チップなどのエミュレーションによる演奏ツール)  
  
[概要]  
  このツールは、鍵盤表示を行いながらVGMファイルの再生を行います。  
  (NRD,XGM,S98,MID,RCP,NSF,HES,SID,MDRファイルにも対応。)  
  
[注意]  
  ・再生時の音量に注意してください。バグによる雑音が大音量で再生される場合もあります。  
  (特に再生したことのないファイルを試す場合や、プログラムを更新した場合。)  
  
  ・使用中に不具合を見つけた場合はお手数ですがTwitter(@kumakumakumaT_T)までご連絡ください。  
  (VGMPlayやNRTDRV、その他素晴らしいソフトウェアの作者様方に、  
  直接MDPlayerについての連絡がいくことの無い様にお願いします。)  
  できるかぎり対応させていただくつもりですが、ご希望に添えないことも多々あります。ご了承ください。  
  
[対応フォーマット]  
  .VGM (所謂vgmファイル)  
  .VGZ (vgmファイルをgzipしたもの)  
  .NRD (NRTDRV X1でOPM2個とAY8910を鳴らすドライバの演奏ファイル)  
  .XGM (MegaDrive向けファイル)  
  .S98 (主に日本製レトロPC向けファイル)  
  .MID (StandardMIDIファイル。フォーマット0/1対応)  
  .RCP (レコポンファイル CM6,GSDの送信可)  
  .NSF (NES Sound Format)  
  .HES (HESファイル)  
  .SID (コモドール向けファイル)  
  .MDR (MoonDriver MSX,MoonSoundでOPL4を鳴らすドライバの演奏ファイル)  
  .MDX (MXDRV向けファイル)
  .M3U (プレイリスト)  
  
[機能、特徴]  
  ・現在、以下の主にメガドライブ系音源チップのエミュレーションによる再生が可能です。  
     
      AY8910    , YM2612   , SN76489 , RF5C164 , PWM     , C140(C219) , OKIM6258 , OKIM6295  
      , SEGAPCM , YM2151   , YM2203  , YM2413  , YM2608  , YM2610/B   , HuC6280  , C352  
      , K054539 , NES_APU  , NES_DMC , NES_FDS , MMC5    , FME7       , N160     , VRC6  
      , VRC7    , MultiPCM , YMF262  , YMF271  , YMF278B , YMZ280B    , DMG      , QSound  
      , SID  
  
  ・現在、以下の鍵盤表示が可能です。  
     
      AY8910    , YM2612   , SN76489  , RF5C164 , C140(C219) , SEGAPCM     , YM2151  , YM2203  
      , YM2413  , YM2608   , YM2610/B , HuC6280 , MIDI       , NES_APU&DMC , NES_FDS , MMC5  
  
  ・C#で作成されています。  
  
  ・VGMPlayのソースを参考、移植しています。  
  
  ・FMGenのソースを参考、移植しています。  
  
  ・NSFPlayのソースを参考、移植しています。  
  
  ・NEZ Plug++のソースを参考、移植しています。  
  
  ・libsidplayfpのソースを参考、移植しています。  
  
  ・sidplayfpのソースを参考、移植しています。  
  
  ・NRTDRVのソースを参考、移植しています。  
  
  ・MoonDriverのソースを参考、移植しています。  
  
  ・MXPのソースを参考、移植しています。  
  
  ・MXDRVのソースを参考、移植しています。  
  
  ・CVS.EXEの出力を参考に同じデータが出力されるよう調整しています。  
  
  ・SCCIを利用して本物のYM2612,SN76489,YM2608,YM2151から再生が可能です。  
  
  ・ボタンは以下の順に並んでいます。  
     
     設定、停止、一時停止、フェードアウト、前の曲、1/4速再生、再生、4倍速再生、次の曲、  
     プレイモード、ファイルを開く、プレイリスト、  
     情報パネル表示、ミキサーパネル表示、パネルリスト表示、VSTeffectの設定、MIDI鍵盤表示、表示倍率変更  
  
  ・チャンネル(鍵盤)を左クリックすることでマスクさせることができます。  
    右クリックすると全チャンネルのマスクを解除します。  
    (いろいろなレベルで対応していないのもあり)  
  
  ・OPN,OPM,OPL系の音色パラメーターを左クリックするとクリップボードに音色パラメーターをテキストとしてコピーします。  
  パラメーターの形式はオプション設定から変更可能です。  
     
      FMP7 , MDX , MUSIC LALF , NRTDRV , HuSIC , MML2VGM , .TFI , MGSC  
  
  に対応しており、.TFIを選んだ場合はクリップボードの代わりにファイルに出力します。  
  
  ・出来は今一歩ですが、YM2612 , YM2151 の演奏データをMIDIファイルとして出力が可能です。  
  VOPMexを使用すれば、FM音源の音色情報も反映させることが可能です。  
  (VOPMではなく、VOPMexです。;-P )  
  
  ・PCMデータをダンプすることができます。SEGAPCMの場合のみWAVで出力します。  
  
  ・演奏をwavで書き出すことが可能です。  
  
  ・MIDI音源にVSTiを指定可能です。  
  
  
[必要な動作環境]  
  ・恐らく、WindowsVista(32bit)以降のOS。64ビット環境では未検証。  
  XPでは動作しません。  
  
  ・.NET Framework4.5/4.5.2をインストールしている必要あり。  
  
  ・Visual Studio 2012 更新プログラム 4 の Visual C++ 再頒布可能パッケージ をインストールしている必要あり。  
  
  ・Microsoft Visual C++ 2015 Redistributable(x86) - 14.0.23026をインストールしている必要あり。  
  
  ・LZHファイルを使用する場合はUNLHA32.DLL(Ver3.0以降)をインストールしている必要あり。  
  
  ・音声を再生できるオーディオデバイスが必須。  
  そこそこ性能があるものが必要です。UMX250のおまけでついてたUCA222でも十分いけます。私はこれ使ってます。  
  
  ・もしあれば、SPFM Light＋YM2612＋YM2608＋YM2151  
  
  ・YM2608のエミュレーション時、リズム音を鳴らすために以下の音声ファイルが必要です。  
  作成方法は申し訳ありませんがお任せします。  
      
      バスドラム      2608_BD.WAV  
      ハイハット      2608_HH.WAV  
      リムショット    2608_RIM.WAV  
      スネアドラム    2608_SD.WAV  
      タムタム        2608_TOM.WAV  
      トップシンバル  2608_TOP.WAV  
      (44.1KHz 16bitPCM モノラル 無圧縮Microsoft WAVE形式ファイル)  
  
  ・YMF278Bのエミュレーション時、MoonSoundの音色を鳴らすために以下のROMファイルが必要です。  
  作成方法は申し訳ありませんがお任せします。  
  	yrw801.rom  
  
  ・C64のエミュレーション時、Kernal,Basic,CharacterのROMファイルが必要です。  
  作成方法は申し訳ありませんがお任せします。  
  
  ・そこそこ速いCPU。  
  
  
[SpecialThanks]  
  本ツールは以下の方々にお世話になっております。また以下のソフトウェア、ウェブページを参考、使用しています。  
  本当にありがとうございます。  
     
    ・ラエル さん  
    ・とぼけがお さん  
    ・HI-RO さん  
    ・餓死3 さん  
    ・おやぢぴぴ さん  
    ・osoumen さん  
    ・なると さん  
    ・hex125 さん  
    ・Kitao Nakamura さん  
    ・くろま さん  
    ・かきうち さん  
    ・ぼう☆きち さん  
    ・dj.tuBIG/MaliceX さん  
     
    ・Visual Studio Community 2015  
    ・MinGW/msys  
    ・SGDK  
    ・VGM Player  
    ・Git  
    ・SourceTree  
    ・さくらエディター  
    ・VOPMex  
    ・NRTDRV  
    ・MoonDriver  
    ・MXP  
    ・MXDRV  
    ・hoot  
    ・ASLPLAY  
    ・NAUDIO  
    ・VST.NET  
    ・NSFPlay  
    ・CVS.EXE  
     
    ・SMS Power!  
    ・DOBON.NET  
    ・Wikipedia  
    ・GitHub  
  
[同期のすゝめ]  
      
  ・SCCIとエミュレーション(以下EMUと略す)による音を同期させるのにはコツがいります。  
  環境にもよるので何が正解かはわからないのですが、私の環境での調整手順を紹介します。  
      
    １．まず、[出力]タブから音声の出力に使用するデバイスを選びます。  
    おすすめはWasapiOutで共有を選ぶ、又はASIOを選ぶパターンです。  
      
    ２．遅延時間は50msか100msを選びます。ここで一度[OK]を押してEMUのみを使用する曲を再生し  
    音がざらざらしたりプチプチといったノイズが混ざらないことを確認します。  
    (もし綺麗に再生されない場合は遅延時間をひとつ大きく設定します。)  
      
    ３．[音源]タブからYM2612のSCCIを選択し使用するモジュールを選択します。  
    チェックボックスは「Waitシグナル発信」と「PCMだけエミュレーションする」にチェックを入れてください。  
    「Waitシグナル発信」を行うとSCCIのテンポが安定するようです。  
    しかし「そのWait値を2倍」にチェックするとPCMの音質は上がりますがテンポが乱れる傾向があります。  
      
    ４．遅延演奏のグループはとりあえずSCCIもEMUも0msを設定し「日和見モード」にはチェックをいれてください。  
    「日和見モード」は、例えば演奏中に大きな負荷がかかり、SCCIの再生とEMUの再生が大きくずれた場合に  
    SCCIの再生スピードを調整してズレを軽減させる機能です。但し、遅延演奏で設定した(意図した)ズレは保ち続けます。  
      
    ５．SCCIとEMUの両方が使用されている曲を再生し、どちらが先に鳴っているか注意深く確認します。  
    SCCIとEMUのうち先に演奏されている方の遅延演奏時間を増やし曲再生を行い確認します。  
      
    ６．５の手順をズレがなくなるまで繰り返せば同期作業は完了です。楽しんで！  
  
[MIDI鍵盤のすゝめ]  
  ・MIDIキーボードを用意すると、それを使用してYM2612(EMU)から発音させることができます。  
  これは主にMML打ち込み支援のために用意された機能です。  
  (今のところ実装途中の状態で使用できない機能があります。)  
  
  ・とりあえずの使い方  
      
    １. 設定画面で、使用するMIDIキーボードを選択します。  
       
    ２. YM2612のデータを再生中に(CC:97)を送信します。  
        YM2612の1Chの音色が全てのチャンネルへセットされます。  
       
    ３. 後は弾くだけ。  
    
  ・主な機能  
      
    １. 音色データ取り込み  
      各OPN系音源又はOPM音源、鍵盤表示の音色データ部をクリックすると  
      音色データが選択チャンネルへコピーされます。  
      
    ２. 演奏モード切替  
      MONOモード(単一チャンネルを使用して演奏)と  
      POLYモード(複数チャンネル(最大6Ch)を使用して演奏)を  
      切り替えることが可能です。  
      MONOモード  打ち込み時に短いフレーズを演奏し、MMLとして出力することを想定。  
      POLYモード  打ち込み時に和音を確認するために使用することを想定。  
      
    ３. チャンネルノートログ  
      チャンネルごとに最大100音の発音記録を残すことができます。  
      
    ４. チャンネルノートログMML変換機能  
      ログ欄をクリックすると発音記録がMMLとしてクリップボードにコピーされます。  
      音長は出力されません。対応コマンドは、c d e f g a b o < > です。  
      オクターブ情報は初めの一音のみoコマンドで絶対指定され、  
      その後は<コマンド>コマンドによる相対指定で展開されます。  
      
    ５. 音色保存、読み込み  
      メモリ上に256種類の音色を保存する、又は読み込むことが可能です。  
      そのデータを、指定された形式でファイルへ出力する、又は読み込むことが可能です。  
      以下のソフトウェア向けの形式で保存、読み込みが可能です。  
        FMP7  
        MUSIC LALF  
        NRTDRV  
        MXDRV  
        mml2vgm  
      
    ６. 簡易音色編集(TBD)  
      入力したいパラメータを選択後、数値を入力することで編集が可能です。  
      
  ・画面  
      
    ０. 鍵盤(TBD)  
      演奏中のノートが表示されます。  
      
    １．MONO  
      クリックするとMONOモードに切り替えます。切り替わると♪アイコンになります。  
      
    ２．POLY  
      クリックするとPOLYモードに切り替えます。切り替わると♪アイコンになります。  
      
    ３．PANIC  
      全チャンネルにキーオフを送信します。(音が鳴り続けてしまう場合に使用します。)  
      
    ４．L.CLS  
      全チャンネルのノートログをクリアします。  
      
    ５．TP.PUT  
      TonePallet(メモリ上の音色保管領域)へ選択チャンネルの音色を保存します。  
      
    ６．TP.GET  
      TonePalletから音色を選択チャンネルへ読み込みます。  
      
    ７．T.SAVE  
      TonePalletをファイルへ保存します。  
      
    ８．T.LOAD  
      ファイルからTonePalletを読み込みます。  
      
    ９．音色データ(6Ch分)  
      ・「-」又は「♪」をクリックすることで チャンネルの選択、選択解除ができます。  
      ・パラメータを右クリックすることで、そのパラメータの変更ができます。(TBD)  
      ・パラメータを左クリックすることで、コンテキストメニューが表示されます。  
        コピー   : クリックした音色をクリップボードにコピーします。  
        貼り付け : クリップボードの音色をクリックした音色にペーストします。  
        上記の機能で使用されるテキスト形式をFORMATの欄のソフトウェア名をクリックすることで変更できます。  
        キー操作でもコピーと貼り付けが可能です。この場合は選択されているチャンネルが対象になります。  
        尚、貼り付け時に形式の自動判別は行われません。  
      ・「LOG」の隣の「♪」をクリックすることで、そのチャンネルのノートログがクリアされます。  
      ・LOGをクリックすることで、MMLデータをクリップボードに設定します。  
      
  ・MIDI鍵盤からの操作  
    以下、デフォルト設定の場合です。(設定でカスタマイズ可能。設定値をブランクにすることで使用しないことも可能。)  
      
    CC:97(DATA DEC)  
      YM2612の1Chの音色を全てのチャンネルにコピーします。(選択状況無視)  
      
    CC:96(DATA INC)  
      直近のログをひとつだけ削除します。(弾き間違い等を取り消す機能)  
      
    CC:66(SOSTENUTO)  
      MONOモード時のみ、選択行のログをMMLにしクリップボードに設定します。  
      画面クリック時の処理との違い
        Ctrl+V（ペースト）のキーストロークを送信します。  
        選択チャンネルのノートログをクリアします。  
        初めのオクターブコマンドは出力しません。  
      
  
