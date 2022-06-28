/*
 * https://github.com/naudio/NAudio/blob/fb35ce8367f30b8bc5ea84e7d2529e172cf4c381/NAudio.Core/Manufacturers.cs
 */

package mdplayer;

import java.util.Arrays;


/**
 * Manufacturer codes from mmreg.h
 */
public enum Manufacturers {
	/** Microsoft Corporation */
	Microsoft(1 ,"Microsoft Corporation"),
	/** Creative Labs, Inc */
	Creative(2 ,"Creative Labs, Inc."),
	/** Media Vision, Inc. */
	Mediavision(3 ,"Media Vision, Inc."),
	/** Fujitsu Corp. */
	Fujitsu(4 ,"Fujitsu, Ltd."),
	/** Artisoft, Inc. */
	Artisoft(20, "Artisoft, Inc."),
	/** Turtle Beach, Inc. */
	TurtleBeach(21 ,"Turtle Beach Systems"),
	/** IBM Corporation */
	Ibm(22 ,"International Business Machines"),
	/** Vocaltec LTD. */
	Vocaltec(23 ,"VocalTec, Inc."),
	/** Roland */
	Roland(24 ,"Roland Corporation"),
	/** DSP Solutions, Inc. */
	DspSolutions(25 ,"DSP Solutions, Inc."),
	/** NEC */
	Nec(26 ,"NEC Corporation"),
	/** ATI */
	Ati(27 ,"ATI Technologies, Inc."),
	/** Wang Laboratories, Inc */
	Wanglabs(28, "Wang Laboratories"),
	/** Tandy Corporation */
	Tandy(29 ,"Tandy Corporation"),
	/** Voyetra */
	Voyetra(30 ,"Voyetra Technologies"),
	/** Antex Electronics Corporation */
	Antex(31, "Antex Electronics Corporation"),
	/** ICL Personal Systems */
	IclPS(32 ,"ICL Personal Systems"),
	/** Intel Corporation */
	Intel(33 ,"Intel Corporation"),
	/** Advanced Gravis */
	Gravis(34, "Advanced Gravis Computer Technology, Ltd."),
	/** Video Associates Labs, Inc. */
	Val(35 ,"Video Associates Labs, Inc."),
	/** InterActive Inc */
	Interactive(36 ,"InterActive, Inc."),
	/** Yamaha Corporation of America */
	Yamaha(37, "Yamaha Corporation of America"),
	/** Everex Systems, Inc */
	Everex(38 ,"Everex Systems, Inc."),
	/** Echo Speech Corporation */
	Echo(39 ,"Echo Speech Corporation"),
	/** Sierra Semiconductor Corp */
	Sierra(40 ,"Sierra Semiconductor Corporation"),
	/** Computer Aided Technologies */
	Cat(41 ,"Computer Aided Technology, Inc."),
	/** APPS Software International */
	Apps(42, "APPS Software"),
	/** DSP Group, Inc */
	DspGroup(43 ,"DSP Group, Inc."),
	/** microEngineering Labs */
	Melabs(44 ,"microEngineering Labs"),
	/** Computer Friends, Inc. */
	ComputerFriends(45 ,"Computer Friends, Inc."),
	/** ESS Technology */
	Ess(46 ,"ESS Technology, Inc."),
	/** Audio, Inc. */
	Audiofile(47 ,"Audio, Inc."),
	/** Motorola, Inc. */
	Motorola(48 ,"Motorola, Inc."),
	/** Canopus, co., Ltd. */
	Canopus(49 ,"Canopus, Co., Ltd."),
	/** Seiko Epson Corporation */
	Epson(50 ,"Seiko Epson Corporation, Inc."),
	/** Truevision */
	Truevision(51 ,"Truevision, Inc."),
	/** Aztech Labs, Inc. */
	Aztech(52 ,"Aztech Labs, Inc."),
	/** Videologic */
	Videologic(53,"VideoLogic, Inc."),
	/** SCALACS */
	Scalacs(54 ,"SCALACS"),
	/** Korg Inc. */
	Korg(55 ,"Toshihiko Okuhura, Korg, Inc."),
	/** Audio Processing Technology */
	Apt(56 ,"Audio Processing Technology"),
	/** Integrated Circuit Systems, Inc. */
	Ics(57 ,"Integrated Circuit Systems, Inc."),
	/** Iterated Systems, Inc. */
	Iteratedsys(58 ,"Iterated Systems, Inc."),
	/** Metheus */
	Metheus(59 ,"Metheus Corporation"),
	/** Logitech, Inc. */
	Logitech(60,"Logitech, Inc."),
	/** Winnov, Inc. */
	Winnov(61, "Winnov, LP"),
	/** NCR Corporation */
	Ncr(62 ,"NCR Corporation"),
	/** EXAN */
	Exan(63 ,"EXAN, Ltd."),
	/** AST Research Inc. */
	Ast(64 ,"AST Research, Inc."),
	/** Willow Pond Corporation */
	Willowpond(65, "Willow Pond Corporation"),
	/** Sonic Foundry */
	Sonicfoundry(66 ,"Sonic Foundry"),
	/** Vitec Multimedia */
	Vitec(67 ,"Visual Information Technologies, Inc."),
	/** MOSCOM Corporation */
	Moscom(68 ,"MOSCOM Corporation"),
	/** Silicon Soft, Inc. */
	Siliconsoft(69 ,"Silicon Software, Inc."),
	/** Supermac */
	Supermac(73 ,"Supermac Technology, Inc."),
	/** Audio Processing Technology */
	Audiopt(74 ,"Audio Processing Technology"),
	/** Speech Compression */
	Speechcomp(76 ,"Speech Compression"),
	/** Ahead, Inc. */
	Ahead(77, "Ahead, Inc"),
	/** Dolby Laboratories */
	Dolby(78 ,"Dolby Laboratories, Inc."),
	/** OKI */
	Oki(79 ,"OKI"),
	/** AuraVision Corporation */
	Auravision(80 ,"Auravision Corporation"),
	/** Ing C. Olivetti &amp; C., S.p.A. */
	Olivetti(81 ,"Ing. C. Olivetti & C., S.p.A."),
	/** I/O Magic Corporation */
	Iomagic(82 ,"I/O Magic Corporation"),
	/** Matsushita Electric Industrial Co., LTD. */
	Matsushita(83 ,"Matsushita Electric Corporation of America"),
	/** Control Resources Limited */
	Controlres(84 ,"Control Resources Corporation"),
	/** Xebec Multimedia Solutions Limited */
	Xebec(85, "Xebec Multimedia Solutions Limited"),
	/** New Media Corporation */
	Newmedia(86 ,"New Media Corporation"),
	/** Natural MicroSystems */
	Nms(87 ,"Natural MicroSystems Corporation"),
	/** Lyrrus Inc. */
	Lyrrus(88 ,"Lyrrus, Inc."),
	/** Compusic */
	Compusic(89 ,"Compusic"),
	/** OPTi Computers Inc. */
	Opti(90 ,"OPTi, Inc."),
	/** Adlib Accessories Inc. */
	Adlacc(91, "Adlib Accessories Inc."),
	/** Compaq Computer Corp. */
	Compaq(92, "Compaq Computer Corp"),
	/** Dialogic Corporation */
	Dialogic(93 ,"Dialogic Corporation"),
	/** InSoft, Inc. */
	Insoft(94, "Insoft"),
	/** M.P. Technologies, Inc. */
	Mptus(95, "M.P. Technologies, Inc."),
	/** Weitek */
	Weitek(96, "Weitek"),
	/** Lernout &amp; Hauspie */
	LernoutAndHauspie(97, "Lernout & Hauspie"),
	/** Quanta Computer Inc. */
	Qciar(98, "Quanta Computer Inc."),
	/** Apple Computer, Inc. */
	Apple(99, "Apple Computer, Inc."),
	/** Digital Equipment Corporation */
	Digital(100, "Digital Equipment Corporation"),
	/** Mark of the Unicorn */
	Motu(101, "Mark of the Unicorn"),
	/** Workbit Corporation */
	Workbit(102, "Workbit Corporation"),
	/** Ositech Communications Inc. */
	Ositech(103, "Ositech Communications Inc."),
	/** miro Computer Products AG */
	Miro(104, "miro Computer Products AG"),
	/** Cirrus Logic */
	Cirruslogic(105, "Cirrus Logic"),
	/** ISOLUTION  B.V. */
	Isolution(106, "ISOLUTION  B.V."),
	/** Horizons Technology, Inc */
	Horizons(107, "Horizons Technology, Inc"),
	/** Computer Concepts Ltd */
	Concepts(108, "Computer Concepts Ltd"),
	/** Voice Technologies Group, Inc. */
	Vtg(109, "Voice Technologies Group, Inc."),
	/** Radius */
	Radius(110, "Radius"),
	/** Rockwell International */
	Rockwell(111, "Rockwell International"),
	/** Co. XYZ for testing */
	Xyz(112, "Co. XYZ for testing"),
	/** Opcode Systems */
	Opcode(113, "Opcode Systems"),
	/** Voxware Inc */
	Voxware(114, "Voxware Inc"),
	/** Northern Telecom Limited */
	NorthernTelecom(115, "Northern Telecom Limited"),
	/** APICOM */
	Apicom(116, "APICOM"),
	/** Grande Software */
	Grande(117, "Grande Software"),
	/** ADDX */
	Addx(118, "ADDX"),
	/** Wildcat Canyon Software */
	Wildcat(119, "Wildcat Canyon Software"),
	/** Rhetorex Inc */
	Rhetorex(120, "Rhetorex Inc"),
	/** Brooktree Corporation */
	Brooktree(121, "Brooktree Corporation"),
	/** ENSONIQ Corporation */
	Ensoniq(125, "ENSONIQ Corporation"),
	/** FAST Multimedia AG */
	Fast(126, "FAST Multimedia AG"),
	/** NVidia Corporation */
	Nvidia(127, "NVidia Corporation"),
	/** OKSORI Co., Ltd. */
	Oksori(128, "OKSORI Co., Ltd."),
	/** DiAcoustics, Inc. */
	Diacoustics(129, "DiAcoustics, Inc."),
	/** Gulbransen, Inc. */
	Gulbransen(130, "Gulbransen, Inc."),
	/** Kay Elemetrics, Inc. */
	KayElemetrics(131, "Kay Elemetrics, Inc."),
	/** Crystal Semiconductor Corporation */
	Crystal(132, "Crystal Semiconductor Corporation"),
	/** Splash Studios */
	SplashStudios(133, "Splash Studios"),
	/** Quarterdeck Corporation */
	Quarterdeck(134, "Quarterdeck Corporation"),
	/** TDK Corporation */
	Tdk(135, "TDK Corporation"),
	/** Digital Audio Labs, Inc. */
	DigitalAudioLabs(136, "Digital Audio Labs, Inc."),
	/** Seer Systems, Inc. */
	Seersys(137, "Seer Systems, Inc."),
	/** PictureTel Corporation */
	Picturetel(138, "PictureTel Corporation"),
	/** AT&amp;T Microelectronics */
	AttMicroelectronics(139, "AT&T Microelectronics"),
	/** Osprey Technologies, Inc. */
	Osprey(140, "Osprey Technologies, Inc."),
	/** Mediatrix Peripherals */
	Mediatrix(141, "Mediatrix Peripherals"),
	/** SounDesignS M.C.S. Ltd. */
	Soundesigns(142, "SounDesignS M.C.S. Ltd."),
	/** A.L. Digital Ltd. */
	Aldigital(143, "A.L. Digital Ltd."),
	/** Spectrum Signal Processing, Inc. */
	SpectrumSignalProcessing(144, "Spectrum Signal Processing, Inc."),
	/** Electronic Courseware Systems, Inc. */
	Ecs(145, "Electronic Courseware Systems, Inc."),
	/** AMD */
	Amd(146, "AMD"),
	/** Core Dynamics */
	Coredynamics(147, "Core Dynamics"),
	/** CANAM Computers */
	Canam(148, "CANAM Computers"),
	/** Softsound, Ltd. */
	Softsound(149, "Softsound, Ltd."),
	/** Norris Communications, Inc. */
	Norris(150, "Norris Communications, Inc."),
	/** Danka Data Devices */
	Ddd(151, "Danka Data Devices"),
	/** EuPhonics */
	Euphonics(152, "EuPhonics"),
	/** Precept Software, Inc. */
	Precept(153, "Precept Software, Inc."),
	/** Crystal Net Corporation */
	CrystalNet(154, "Crystal Net Corporation"),
	/** Chromatic Research, Inc */
	Chromatic(155, "Chromatic Research, Inc"),
	/** Voice Information Systems, Inc */
	Voiceinfo(156, "Voice Information Systems, Inc"),
	/** Vienna Systems */
	Viennasys(157, "Vienna Systems"),
	/** Connectix Corporation */
	Connectix(158, "Connectix Corporation"),
	/** Gadget Labs LLC */
	Gadgetlabs(159, "Gadget Labs LLC"),
	/** Frontier Design Group LLC */
	Frontier(160, "Frontier Design Group LLC"),
	/** Viona Development GmbH */
	Viona(161, "Viona Development GmbH"),
	/** Casio Computer Co., LTD */
	Casio(162, "Casio Computer Co., LTD"),
	/** Diamond Multimedia */
	Diamondmm(163, "Diamond Multimedia"),
	/** S3 */
	S3(164, "S3"),
	/** Fraunhofer */
	FraunhoferIis(172, "Fraunhofer");
	public final int id;
	public final String manufacture;
	Manufacturers(int id, String manufacture) {
		this.id = id;
		this.manufacture = manufacture;
	}
	public static Manufacturers byId(int id) {
		return Arrays.stream(values()).filter(e -> e.id == id).findFirst().get();
	}
	public static Manufacturers byManufacture(String manufacture) {
		return Arrays.stream(values()).filter(e -> e.manufacture.equals(manufacture)).findFirst().get();
	}
}