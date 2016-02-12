package org.bitbucket.eunjeon.seunjeon

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.io.Source


class AnalyzerTest extends FunSuite with BeforeAndAfter {
  before {
//    Analyzer.resetUserDict()
  }

  test("main test") {
    // FIXME: 분석이 이상하게 나옴.
    Analyzer.parse("하늘을 나는 자동차.").foreach(println)
    // TODO: double-array-trie library bug.
    //    Analyzer.parse("모두의마블\uffff전설의 5시간 및 보석 교체").foreach(println)
  }

  test("penalty cost") {
    assert("아버지,가,방,에,들어가,신다,." ==
      Analyzer.parse("아버지가방에들어가신다.").map(_.morpheme.surface).mkString(","))
    assert("아버지,가방,에,들어가,신다,." ==
      Analyzer.parse("아버지 가방에 들어가신다.").map(_.morpheme.surface).mkString(","))
  }

  test("number test") {
    Analyzer.parse("12345한글67890 !@# ABCD").foreach { t: LNode =>
      println(t)
    }
  }

  test("userdic-surface from file") {
    assert(Seq(
      "버:NNP",
      "카:NNG",
      "충:NNG",
      "했:XSV+EP",
      "어:EF", "?:SF") == Analyzer.parse("버카충했어?").map(getSurfacePos))
    Analyzer.setUserDictDir("src/test/resources/userdict/")
    assert(Seq(
      "버카충:NNG",
      "했:XSV+EP",
      "어:EF",
      "?:SF") == Analyzer.parse("버카충했어?").map(getSurfacePos))
  }

  test("userdic-surface from iterator") {
    assert(Seq(
      "어:IC",
      "그:NP",
      "로:JKB",
      "좀:MAG",
      "끌:VV",
      "고:EC",
      "있:VX",
      "어:EC",
      "봐:VX+EF",
      ".:SF") == Analyzer.parse("어그로좀끌고있어봐.").map(getSurfacePos))
    Analyzer.setUserDict(Seq("어그로,-500", "갠소").toIterator)
    assert(Seq(
      "어그로:NNG",
      "좀:MAG",
      "끌:VV",
      "고:EC",
      "있:VX",
      "어:EC",
      "봐:VX+EF",
      ".:SF") == Analyzer.parse("어그로좀끌고있어봐.").map(getSurfacePos))
  }

  test("multi-char-dict") {
    Analyzer.setUserDict(Seq("삼성SDS", "LG CNS").toIterator)
    assert(Seq(
      "삼성SDS:NNG") == Analyzer.parse("삼성SDS").map(getSurfacePos))
    assert(Seq(
      "LG CNS:NNG") == Analyzer.parse("LG CNS").map(getSurfacePos))
  }

  test("README example1") {
    Analyzer.parse("아버지가방에들어가신다.").foreach(println)
  }

  test("READ example2") {
    println("# BEFORE")
    Analyzer.parse("덕후냄새가 난다.").foreach(println)
    Analyzer.setUserDictDir("src/test/resources/userdict/")
    println("# AFTER ")
    Analyzer.parse("덕후냄새가 난다.").foreach(println)
  }

  test("README example3") {
    println("# BEFORE")
    Analyzer.parse("덕후냄새가 난다.").foreach(println)
    Analyzer.setUserDict(Seq("덕후", "버카충,-100", "낄끼빠빠").toIterator)
    println("# AFTER ")
    Analyzer.parse("덕후냄새가 난다.").foreach(println)
  }

  test("README eojeol") {
    Analyzer.parseEojeol("아버지가방에들어가신다.").map(_.surface).foreach(println)
    Analyzer.parseEojeol(Analyzer.parse("아버지가방에들어가신다.")).foreach(println)
  }

  test("empty eojeol") {
    assert(Seq[Eojeol]() == Analyzer.parseEojeol(""))
  }

  test("dePreAnalysis") {
    val result1 = Analyzer.parse("은전한닢프로젝트")
    assert("은전+한+닢+프로젝트" == result1.map(_.morpheme.surface).mkString("+"))
    result1.foreach(println)

    val result2 = Analyzer.parse("은전한닢프로젝트", preAnalysis = false)
    assert("은전한닢+프로젝트" == result2.map(_.morpheme.surface).mkString("+"))
    result2.foreach(println)
  }

  test("functation") {
    Analyzer.parse("""F = \frac{10^7}{(4\\pi)^2}""").foreach(println)
    Analyzer.parse("《재규어》.").foreach(println)
    Analyzer.parse(" ^^ 55 《삐리리~ 불어봐! 재규어》.").foreach(println)
  }

  test("multi line") {
    Analyzer.parse("가\n나").foreach(println)
  }

  test("long text") {
    val longText = Source.fromInputStream(getClass.getResourceAsStream("/path_disconnect.txt")).mkString
    val morphemes = Analyzer.parse(longText)
    morphemes.foreach(println)
    assert("\"" == morphemes.head.morpheme.surface)
    assert("오늘밤" == morphemes(1).morpheme.surface)
    assert("기사" == morphemes.last.morpheme.surface)
  }

  test("disconnected path") {
    val text = """입력 : 2015.07.30 14:24   KT(030200)는 29일(현지시각) 우즈베키스탄 수도 타슈켄트의 인터내셔날 호텔에서 ‘4세대(4G) 롱텀에볼루션(LTE) 상용 서비스 론칭 행사’를 갖고 LTE-TDD 서비스 상용화를 선언했다고 30일 밝혔다.KT는 2007년, 우즈베키스탄 수도인 타슈켄트에서 Super-iMax(브랜드명 EVO)를 설립한 이후 2008년부터 WiMax 서비스를 독점적으로 제공해왔다. 지난 4월부터는 LTE-TDD 시범서비스를 시작했으며, 이를 기반으로 타슈켄트 내 약 98%와 주변 도시 일부에서 EVO LTE 서비스를 성공적으로 개시했다.      ▲  4G LTE 상용 서비스 론칭 행사의 모습. (왼쪽부터) Super iMax 김종삼 사장, 우즈베키스탄 알라모프 우미존(Umidjon B. A’LAMOV) 정보통신부 차관, 김남석 우즈베키스탄 정보통신부 차관, KT 출자경영1담당 문정용 상무가 기념 촬영을 하고 있는 모습. /KT제공   KT는 내년 말까지 우즈베키스탄의 사마르칸트, 부하라와 페르가나벨리의 주요 도시까지 서비스 제공 지역을 확대해 나갈 예정이다. 또한 우즈베키스탄 유선 통신 사업자인 이스트텔레콤(East Telecom)의 네트워크와 연계해 정보통신기술(ICT) 인프라 구축사업을 확장할 계획이다.KT는 이번 우즈베키스탄 LTE-TDD 서비스 상용화를 통해 국내에서는 LTE-FDD를, 국외에서는 LTE-TDD 상용 서비스를 제공하는 국내 최초의 통신사업자가 됐다.문정용 KT 출자경영1담당 상무는 “EVO LTE 서비스가 우즈베키스탄 국민의 삶의 질 향상에 기여할 것으로 기대한다”며 “LTE 서비스 상용화가 우즈베키스탄이 새로운 Broadband 시대를 맞이할 수 있는 계기가 될 것”이라고 말했다.한편 이날 론칭 행사에는 문 상무와 김윤제 유럽·아프리카 담당 상무를 비롯해 이욱현 우즈베키스탄 한국대사, 알라모프 우미존 우즈베키스탄 정보통신부 차관, 사디코프 슈흐랏 우즈베키스탄 내각 정보통신수석 등 우즈베키스탄 정부와 민간 기업 관계자 200여명이 참석했다.
             |[머니투데이 대학경제 권현수 기자] 순천향대학교(총장 서교일)는 지난 6~31일 4주간 상하이에 위치한 한국기업에서 ‘2015 하계 외국인유학생 중국내 한국기업 인턴십 프로그램’을 실시했다고 밝혔다.  이번 인턴십은 중국 국적 8명의 유학생을 대상으로 상하이에 위치한 렛츠고 인터내셔날 등 5개 중국법인에서 4주간 진행했으며, 유학생들의 취업역량강화와 실질적인 취업연계를 겨냥해 운영했다. 특히, 중국 현지에서 수년간 비즈니스를 하고 있는 상하이·화동지역 한국IT기업협의회의 기업인들이 전담 멘토단으로 참여해 학생들을 직접 코칭하고 관리했다.이번 인턴십에 참가한 조맹(曹猛, 국제통상학과 4학년) 학생은 “졸업 후 중국내 한국기업 취업이 목표인데 인턴십 기간동안 실무 경험을 배우고 싶어 지원했다”며, “인턴십을 통해 많이 배워서 원하는 기업에 취업했으면 한다”고 말했다.
             |임시주주총회 결과         1. 결의사항   제 1호 의안 : 정관일부 변경 건 => 원안대로 가결   제 2호 의안 : 이사 선임의 건 <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-1 호 : 사내이사 선임의 건(후보자 오세광) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-2 호 : 사내이사 선임의 건(후보자 신환율) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-3 호 : 사내이사 선임의 건(후보자 전  선) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-4 호 : 사내이사 선임의 건(후보자 이혁수) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-5 호 : 사내이사 선임의 건(후보자 김정상) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결 <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?> <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-6 호 : 사외이사 선임의 건(후보자 Sun Zhen Kun)   <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결 <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>                            <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-7 호 : 사외이사 선임의 건(후보자 노시영) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>    2-8 호 : 사외이사 선임의 건(후보자 박종군) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결  <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>        제 3호 의안 : 감사 선임의 건(후보자 송주환) <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>             => 원안대로 가결   제 4호 의안 : 주식매수선택권 부여 승인의 건 <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>            => 원안대로 가결 <?javax.xml.transform.disable-output-escaping?> <?javax.xml.transform.enable-output-escaping?>     2. 주주총회 일자   2015-08-07     3. 기타 투자판단에 참고할 사항     -     ※관련공시    2015-07-23 주주총회소집공고  2015-07-23 주주총회소집결의          이사선임 세부내역     성명   출생년월   임기   신규선임여부   주요경력(현직포함)     오세광   1965-10   3   신규선임   경희대학교 의과대학 성형외과 박사 현) 오킴스 성형외과 대표원장 현) 오킴스 코스메틱 대표이사     신환율   1984-12   3   신규선임   중국 Shanghai Jiaotong university 졸업 전) 유니아시아 대표이사 현) 한양인터내셔날 대표이사     전선   1975-11   3   신규선임   포항공과대학 대학원 전기제어계측 석사 전) ITX시큐리티 카메라그룹 그룹장 현) 서건 대표이사     이혁수   1971-03   3   신규선임   고려대 에너지환경정책대학원 석사과정 전) 코레시티 부사장 현) 오킴스 하이타오 대표이사     김정상   1973-09   3   신규선임   산업기술대학원 석사 전) 정연플랜트 대표이사 현) 대경 E&G 대표이사           사외이사선임 세부내역     성명   출생년월   임기   신규선임여부   주요경력(현직포함)   이사 등으로 재직 중인 다른 법인명(직위)     Sun Zhen Kun   1975-01   3   신규선임   Wu han university China Hunan KUAI LE Taobao Cultural Communication. co Ltd (Hitao.com) CEO   China Hunan KUAI LE Taobao Cultural Communication. co Ltd (Hitao.com) CEO     노시영   1961-11   3   신규선임   미국 University of Rochester, MBA 석사 전) TBD corporation   -     박종군   1958-04   3   신규선임   서울대학교 분자생물학 박사 현) 원광대학교 분자생물학과 교수 홍삼을 주 재료로 한 화장료 (주)소망화장품과 공동연구 및 특허취득   (주)제노코스(대표이사             감사선임 세부내역     성명   출생년월   임기   신규선임여부   상근여부   주요경력(현직포함)     송주환   1950-04   3   신규선임   상근   서울대학교 법학과 졸업 전) 남부지방검찰청 차장검사 현) 법무법인 청솔 변호사           사업목적 변경 세부내역     구분   내용   이유     사업목적 추가   17) 화장품 제조, 판매 및 이와 관련된 서비스 상품의 매매업 18) 화장품 연구개발, 제조 및 판매, 수출입업 19) 화학제품 연구개발, 제조 및 판매, 수출입업 20) 의약품, 의약외품, 건강 식품 연구개발, 제조 및 판매, 수출입업 21) 의료기기 제조 및 판매, 수출입업 22) 화장품 기계 부품, 화장품 기계 제조 및 판매업 23) 유아용품 및 패션 잡화 유통업 도소매업 24) 화장품 국내외 무역업 25) 온라인 정보제공 및 인터넷 관련 서비스업 26) 홈쇼핑 운영업 45) 공연, 기획 및 흥행사업 46) 판촉물, 캐릭터, 기념품 제작 및 제조업 47) 공연장 및 서비스업 48) 음반판매 도소매업 49) 방송프로그램 제작업 50) 방송 광고 대행업 51) 비디오물 제작업 52) 영상물 제작업 53) 인터넷 TV사업 54) 방송채널 사용사업 55) 국제회의 기획 및 용역대행업 56) 인쇄, 디자인 및 출판기획업 57) 전자출판업 58) 잡지 및 정기 간행 59) 음반 및 기타 음악 기록매체 출판업 60) 음반 및 비디오물 기타 매체 도소매업 61) 컨텐츠 사업(IP 및 CP), 컨텐츠라이센스 판매업 62) 브랜드 및 상표권 등 지적재산권의 라이센스업 63) 종합물품 대여 및 공연시설물 장비임대업 64) 무대 트러스 대여 및 세트 제작업 65) 연예인 및 스포츠 선수 기타 공인 메니지먼트업 66) 영상음반물의 해외 세일즈 및 투자유치업 67) 신인가수 및 연기자의 발굴 및 양성업 68) 소속가수 및 연기자의 초상권 및 캐릭터 산업 69) 여행사 및 기타 여행보조 서비스업 70) 항공권 및 선표 발권 판매업 71) 관광업종에 대한 용역 및 위탁경영사업 72) 요식업, 관광숙박업 및 관광객 이용시설업 73) 전원공급장치 제조 및 도소매업 74) 조명장치 제조 및 도소매업 75) 차량용 영상장치 제조 및 도소매업 76) 위 각호에 직접, 간접적으로 부대되는 용역, 제품의 제조, 판매 및 유통, 컨설팅   -     사업목적 변경   변경전   변경후   이유     8) 전자상거래를 통한 상품, 제품 판매 및 인터넷관련사업  13) 시스템통합 및 소프트웨어 개발,판매업   8) 통신판매업 및 전자상거래업  13) 인터넷 소프트웨어 개발 및 판매 서비스업   -     사업목적 삭제   17) 자동차 수입, 판매업   18) 건축자재 수출입업 및 육상하역업 19) 건축자재 도소매업 20) 자동차부품 및 용품제조, 수입 판매업 21) 주유소 및 관련부대시설 운영업 22) 액화석유가스 충전 및 판매업 23) 충전소 및 관련부대시설 운영업 24) 석유제품 및 관련부산물의 판매, 저 장, 수송 및 수출입업 25) 편의점 운영 및 체인화사업 26) 휴게음식점 운영 및 체인화사업 45) 위 각호에 관련된 통신판매업 46) 위 각호에 관련된 연구용역업  및 기술서비스업 일체 47) 위 각호에 관련된 부대사업 및 투자   -
             |㈜디지털대성(대표 김희선, 068930)이 운영하는 온라인 대입 브랜드 대성마이맥과 비상에듀의 과학탐구 강사 4인이 다음 달 2일(수) 시행되는 9월 평가원 모의고사 대비 과학탐구 모의고사를 선착순 무료 배포한다.여름방학을 맞아 ▲생명과학 한종철 ▲화학 정훈구 ▲지구과학 김지혁 ▲물리 안철우 등 대성마이맥 과학탐구 대표강사 4인은 ‘오.빠 달려- 과학탐구 오십 점까지 빠르게 달려가자!’ 프로모션을 진행하고 있다. 이번에 오픈한 ‘딱! 풀리는 모의고사’는 지난 7월 진행한 ‘취약파트 공략 특강’의 후속 기획이다.‘딱! 풀리는 모의고사’는 평가원, EBS 출제위원 등 수능 전문가가 제작에 함께 참여한 수능에 최적화된 과학탐구 모의고사다. 수능 최신 출제 경향을 반영한 예상문제, 핵심문제, 기출 변형문제를 수록하였다. 생명과학, 화학, 지구과학, 물리 각 과목당 1회분씩 대성마이맥과 비상에듀 홈페이지에서 10일(월)부터 17일(월)까지 매일 밤 10시에 선착순으로 제공하며 모의고사 해설 강의는 18일(화)에 공개한다. 한편, 이벤트 기간 중 홈페이지에 고민을 남기면 추첨을 통해 10명에게 수험생 필수 아이템으로 구성된 IT BOX를 증정한다. IT BOX에는 오답 노트 작성에 필요한 가위, 풀부터 노트, 형광펜, 포스트잇 등 각종 문구류와 시간 관리를 도와줄 타임워치까지 수험생에게 필요한 아이템이 담겨 있다. 자세한 내용은 대성마이맥(www.mimacstudy.com), 비상에듀(www.visangedu.com) 홈페이지에서 확인할 수 있다. (문의: 02-5252-110 / 1544-7390)<이 기사는 본지 편집 방향과 다를 수 있으며, 해당기관에서 제공한 보도 자료입니다.>
             |홍보대행사 프레인글로벌이 지난 28일 이색 보도자료를 냈다. “박용호 전 뮤지컬해븐 대표를 프레인글로벌 뮤지컬 부문 프로듀서로 영입했다”는 내용이다. 또 “올 12월 개막하는 ‘넥스트 투 노멀’을 시작으로 뮤지컬 제작을 시작해 2017년 공연을 목표로 대극장용 뮤지컬도 제작 준비 중”이라고 밝혔다.　뮤지컬 전문 제작사가 아닌 기업들의 뮤지컬 시장 진출이 잇따르고 있다. 특히 ‘티켓 파워’ 연예인이 소속된 연예기획사와 콘텐트 제작 능력이 있는 영화제작사의 행보가 발빠르다. 프레인글로벌도 사업 영역에 연예매니지먼트가 포함된 회사다. 2011년 자회사 ‘프레인TPC’를 세워 류승룡·조은지·류현경·김무열 등과 계약했다. 또 지난해엔 뮤지컬·공연·음반 관련 레이블 ‘포트럭’을 만들어 뮤지컬 스타 옥주현을 영입했다. 김태성 프레인글로벌 상무는 “회사에 매니지먼트와 마케팅을 담당하는 조직이 있어 뮤지컬 제작에 시너지 효과를 발휘할 수 있을 것”이라고 말했다.　연예기획사의 뮤지컬 제작에 포문을 연 곳은 엑소·슈퍼주니어·소녀시대 등이 포진해 있는 SM엔터테인먼트다. 자회사 ‘SM C&C ’를 통해 지난해 ‘싱잉 인 더 레인’을 제작, 서울 흥인동 충무아트홀에서 공연했다. 엑소의 백현, 슈퍼주니어 규현, 소녀시대 써니 등 소속 아이돌 가수를 대거 출연시켜 세간의 주목을 받았다. 두 번째 작품은 오는 9월 4일 서울 한남동 블루스퀘어 삼성카드홀에서 개막하는 ‘인 더 하이츠’다. 뉴욕의 라틴할렘이라 불리는 워싱턴 하이츠를 배경으로 이민자의 삶과 꿈을 랩·힙합·스트리트댄스 등에 담아 보여주는 작품이다. 샤이니의 키, 엑소의 첸, 에프엑스 루나 등 SM 소속 연예인과 SM 자회사 울림엔터테인먼트 소속인 인피니트 장동우와 김성규가 주요 배역을 맡았다. 이지나 연출, 원미솔 음악감독, 박동우 무대디자이너 , 권도경 음향디자이너 등 제작진의 면면도 화려하다.　뮤지컬계 ‘흥행 보증 수표’ 김준수의 소속사 씨제스엔터테인먼트도 제작 바람에 동참했다. 지난해 12월 자회사 ‘씨제스컬처’를 만들어 뮤지컬 ‘데스노트’를 제작했다. 현재 경기도 성남아트센터에서 공연 중인 ‘데스노트’는  57회차 공연이 전석(1회 1700석) 매진되는 경이적인 기록을 세웠다.　영화사 명필름도 뮤지컬 제작에 나섰다. 2001년 개봉한 자사의 영화 ‘와이키키 브라더스’를 동명의 뮤지컬로 만들어 다음달 28일 경기도 파주 명필름아트센터 개관작으로 무대에 올린다. 강태희 명필름 기획실장은 “개봉 때 흥행 승부를 봐야 하는 영화와 달리 뮤지컬은 두고두고 공연할 수 있는 장르여서 매력적”이라며 “뮤지컬 제작은 명필름이 갖고 있는 콘텐트를 다양하게 활용할 수 있는 방법 중 하나”라고 말했다.　이들 업체의 뮤지컬 시장 진출 이면에는 경영난에 시달리는 기존 뮤지컬 제작사의 딱한 처지가 있다. 국내 뮤지컬 시장은 2001년 라이선스 뮤지컬 ‘오페라의 유령’ 성공 이후 급성장해 연간 매출 규모가 3000억원대에 이르지만, 과잉공급과 배우 개런티 상승 등으로 제작사가 수익을 내기 어려운 구조가 됐다. 또 불황 여파로 CJ 등 대기업의 뮤지컬 투자가 지난해부터 크게 줄면서 ‘새 작품 투자 받아 지난 작품 적자 메우는 식’으로 운영했던 제작사들이 휘청거렸다. 중견 제작사인 뮤지컬해븐이 지난해 6월 법정관리에 들어간 뒤 최근 폐업했고, 비오엠코리아의 ‘두 도시 이야기’는 지난해 7월 출연료를 제때 지급 못해 공연이 취소되는 초유의 사태를 빚었다. 기존 뮤지컬 제작사들이 자금난에 허덕이는 동안, 다른 사업으로 여력이 생긴 업체들이 뮤지컬 제작에 뛰어든 것이다. 올 한 해 설앤컴퍼니·오디컴퍼니·에이콤인터내셔날 등 대형 제작사들이 ‘지저스 크라이스트 슈퍼스타’ ‘맨 오브 라만차’ ‘영웅’ ‘명성황후’ 등 기존 히트작의 재공연에 집중하는 사이, 신생 제작사들이 신작 초연에 도전하고 있다.　김종헌 성신여대 문화예술경영학과 교수는 “연예기획사 등이 뮤지컬 제작에 나서는 현상은 현 단계에서 뮤지컬 시장을 살릴 수 있는 유일한 돌파구”라며 “일시적으로 ‘스타 마케팅’이 심해질 수 있지만, 뮤지컬 관객이 늘어나면 결국엔 작품성으로 승부가 나는 쪽으로 시장이 정화될 것”이라고 말했다.  이지영 기자 jylee@joongang.co.kr
             |2016 대입 성공의 키워드는 수시가 될 전망이다. 올해 대학 입시에서 수시모집 비율이 사상 최대치인 67.4%에 이르면서(지난 22일 한국대학교육협의회에서 발표한 ‘2016학년도 수시 모집요강 주요사항’ 발췌) 수험생들은 9월에 시작하는 수시 대비에 만전을 기해야 할 것으로 보인다. 이에 따라 메가스터디, 스카이에듀, 이투스 등 다양한 교육 브랜드에서 수험생을 위해 수시 전략 정보를 제공하는 입시 설명회를 개최한다.입시교육전문 스카이에듀는 오는 15일과 16일, 부산과 서울에서 ‘2016학년도 8월 입시 설명회’를 개최한다고 밝혔다.이번 스카이에듀 입시설명회는 9월에 시작되는 각 대학별 수시 모집을 대비한 전략 및 수시 지원 체크리스트 등 수험생들의 성공적인 수시 전형 대응 전략을 세울 수 있도록 기획됐다.1부에서는 ‘2016학년도 대입 수시 혼돈의 속사정’이란 주제로 수시와 정시 중 어떤 전형이 유리할지 살펴보고 수시 전형 및 논술 지원 전략과 주요 상위권 대학 지원 전략에 대해 알아본다. 2부는 ‘2016학년도 수시 지원 핵심 체크 리스트’로, 학생부 교과 및 종합 전형의 필수 점검사항에 대해 확인하고, 서류 최종 점검 및 유의사항과 마인드 컨트롤 등 수험생 건강관리에 대해 살펴본다.부산 설명회는 오는 8월 15일(토) 오후 2시에 부산 해운대구에 위치한 벡스코 컨벤션홀 205호에서, 서울 설명회는 오는 8월 16일(일) 오후 2시에 서울 강남구에 위치한 코엑스 그랜드볼룸에서 개최된다.이번 입시 설명회 사전 예약자에게는 다양한 경품이 제공되며 댓글이벤트 등 다양한 행사도 이어질 예정이다.스카이에듀 입시설명회에 대한 자세한 사항과 참여 방법은 스카이에듀 홈페이지에서 확인 가능하다.스카이에듀 이상제 부대표는 “8월 중순의 무더운 여름방학은 바야흐로 수능 D-100도 지나고 9월 한국교육과정평가원 모의평가 대비 등 입시 공부에 많이 바쁘겠지만 무엇보다 9월에 시작되는 수시모집에 만전을 기해야 할 시기”라며 “대학별로 제각각인 수많은 수시전형에 어떻게 대비해야 할 지 막막했을 많은 수험생들과 학부모들에게 이번 입시 설명회를 통해 스카이에듀만의 전략적이고 전문적인 수시 정보를 제공할 것이다”고 말했다..한편, 입시교육전문 스카이에듀는 고등 인터넷 강의(이하 인강) 유료사이트 순 이용자 수 집계 결과에서 지난 1월 가장 많이 방문하는 수능 사이트 1위(닐슨코리안클릭 조사, PC-모바일 통합)에 올라 14년 만에 수능업계 1위를 바꿨다고 밝혔다. 또 2014년에는 온라인 성장률 1위(고등 온라인 시장 상장사와 자사의 매출 성장률 비교 시 1위)를 기록했다고 덧붙였다.온라인 일간스포츠
             |영화 ‘아이언맨’의 주인공 토니 스타크는 주인이 있는 곳까지 날아와 자동으로 몸에 착용되는 아이언맨 슈트뿐만 아니라 적과 자유자재로 전투가 가능한 로봇도 만든다. 인간을 대신해 적과 싸우는 로봇이 공상과학영화에서만 가능한 게 아니다.  미국 국방부가 로봇제작사와 공동 개발한 ‘빅 도그(Big Dog)’ 로봇은 이름처럼 네 발로 무거운 짐을 싣고 험준한 지형을 오르내리며 정찰 임무도 수행할 수 있다.  인공지능(AI)으로 스스로 판단해 목표물을 추적하고 공격하는 기능을 가진 로봇을 ‘킬러로봇’이라고 한다. 이런 로봇이 미국 이스라엘 영국 일본은 물론이고 한국에서도 개발되고 있다. 최근 미국의 한 로봇 전문가가 킬러로봇이 현장에 투입된 사례로 비무장지대(DMZ·휴전선을 중심으로 북쪽과 남쪽까지 각각 2km 이내의 구역)를 들어 화제를 모았다. 삼성테크윈(현 한화테크윈)이 개발한 이 정찰용 로봇은 4개의 감시카메라로 움직이는 물체를 알아보며 공격무기도 달려 있다고 한다. 킬러로봇은 사람 대신 암살, 국가 전복(뒤집어엎음) 등 위험한 임무를 수행할 수 있는 최적화 무기라는 주장이 있는 반면 어린이와 군인을 구별할 수 없어 누구나 공격할 수 있는 위험한 존재라는 반대도 만만치 않다. 킬러로봇이 테러범이나 독재자 등에게 넘어갈 경우 엄청난 학살이 벌어질 수 있다는 점은 크게 우려된다. 영화 ‘터미네이터’에서 AI를 가진 주인공 로봇이 인간을 이해해 가는 존재로 그려진 것은 영화니까 가능했다.  영국의 물리학자 스티븐 호킹, ‘아이언맨’의 실제 주인공이자 전기자동차 테슬라의 설립자 일론 머스크, 애플 공동창업자 스티브 워즈니악 등 세계적 학자 사업가 철학자 1000여 명이 킬러로봇의 개발에 반대하고 나섰다.  미국의 공상과학소설가 아이작 아시모프가 소설 ‘파운데이션’에서 만든 ㉠로봇 3원칙은 첫째 로봇은 인간에게 위해(위험과 재해)를 줘서는 안 되며, 둘째 1원칙을 어기지 않는 범위에서 인간의 명령에 복종해야 하고, 셋째 2원칙을 어기지 않는 범위에서 자신을 보호해야 한다는 것이었다. 인간이 킬러로봇을 만들어놓고 로봇의 윤리를 고민하고 있으니 모순적이다. 문제는 로봇의 윤리가 아니라 사람의 윤리가 아닐까.동아일보 7월 29일자 정성희 논설위원 칼럼 재정리칼럼을 읽고 다음 문제를 풀어 보세요.1. 킬러로봇의 개발을 찬성하는 쪽과 반대하는 쪽의 의견을 찾아 각각 써보세요.-찬성:-반대:2. 다음 중 ㉠을 어긴 로봇의 사례를 찾아보세요. ① 창문 밖으로 떨어지는 사람을 손을 뻗어 구한 로봇 A. ② 어린이를 구하기 위해 달려오는 차를 안전하게 막다가 부서진 로봇 B.③ 주인이 냉동 창고에 갇히자 문을 부순 로봇 C.④ 주인의 명령을 받고 어떤 사람을 공격해 다치게 만든 로봇 D. 3. 인공지능 로봇으로 인해 생겨나는 문제점을 막기 위해서는 로봇을 개발할 때 어떤 기준이 있어야 할까요? 내가 생각하는 ‘로봇 개발의 기준’을 주장하는 글을 써보세요.김보민 동아이지에듀 기자 gomin@donga.com
             |이스트스프링자산운용, 신세계인터내셔날 지분 5.14% 취득  머니투데이 최우영 기자 |입력 : 2015.07.31 18:00       기사    소셜댓글(0)   폰트크게 폰트작게           페이스북   트위터 카카오스토리 프린트 E-mail PDF   닫기      Tweet        -->                     -->  신세계인터내셔날 (137,000원 8500 6.6%)은 이스트스프링자산운용코리아가 지난 28일 36만6673주(5.14%)를 1주당 13만1775원에 장내매수해 주요주주가 됐다고 31일 공시했다. <!--
             |백악관 텃밭의 2배 규모… 브로콜리 등 26종 채소 심어 다음 달 충북 괴산에서 열리는 ‘2015괴산유기농산업엑스포’에 버락 오바마 미국 대통령의 부인 미셸 오바마 여사가 백악관에 가꾸는 텃밭을 본뜬 텃밭이 조성된다. 4일 괴산유기농엑스포 조직위원회에 따르면 ‘US First Lady’s Whitehouse Kitchen Garden(미국 대통령 부인 백악관 텃밭)’이라는 명칭으로 조성하는 250m² 규모의 이 텃밭에는 브로콜리와 청경채, 콜라비 등 26종의 채소를 심는다. 백악관 텃밭(140m²)보다 2배 정도 넓은 이 텃밭은 다음 달 18일 엑스포 개막과 함께 관람객에게 공개할 예정이다. 앞서 괴산유기농엑스포 조직위는 6월 24일 마크 리퍼트 주한 미국대사에게 미셸 오바마 여사의 백악관 텃밭 시연을 요청하는 서한을 보냈으며, 지난달 22일 “시연해도 좋다”는 답변을 받았다. 미셸 여사는 2009년 3월 백악관 남쪽에 잔디를 걷어내고 유기농 채소를 재배하기 시작해 건강한 먹거리 홍보대사로서 미국에 유기농 열풍을 불게 했다. 괴산유기농엑스포 조직위 관계자는 “청소년 비만 퇴치와 유기농 채소 재배 등에 관심이 많은 미셸 여사의 텃밭이 괴산엑스포의 주제와 맞아 협조를 요청했다”고 밝혔다. 세계 첫 유기농 엑스포는 ‘생태적 삶-유기농이 시민을 만나다’를 주제로 9월 18일부터 10월 11일까지 24일간 괴산군 괴산읍 동진천 일원에서 열린다. 기획재정부는 이 행사를 국제 행사로 승인했다. 괴산엑스포에서는 10개의 주전시관이 설치, 운영된다. 2007년 전국에서 처음으로 친환경 농업 군(郡)을 선포한 괴산군은 600여 농가(재배 면적 500ha)가 친환경 인증을 받는 등 유기농 중심지로 성장했다. 2015organic-expo.kr장기우 기자 straw825@donga.com
             |프랜차이즈 업체들이 한 자리에 모여 예비창업자들과 만나는  대규모 프랜차이즈 창업 관련 전시회로 유명한 "제40회 프랜차이즈 창업  박람회(Coex) 2015"가 삼성동 코엑스에서 오늘 30일(목)부터 오는 7월1일(토)까지 진행된다.▲ 제39회 프랜차이즈 창업박람회 모습 (사진=강동완 기자)▲ ㈜ YHB (YHB) ▲ ㈜아이윈엔터프라이즈	(점프노리) ▲ 사과나무주식회사	(커피베이) ▲ ㈜굿투비 (불소식당) ▲ 온스캔스주식회사 (온스캔스) ▲ 맥스원이링크	(셀렉토커피) ▲ ㈜빅투(그램그램)  ▲ 유니웰주식회사 (유니웰)  ▲ 한빛아트(한빛아트)  ▲ 바른창업	(순남시래기)  ▲ 지니네트웍스 (지니네트웍스)  ▲ 압구정봉구비어 (봉구비어)  ▲ 브알라크리머리 (브알라)  ▲ 크레이저 커피 그룹 (크레이저커피)  ▲ 맥도날드	(맥도날드) ▲ 그린엔푸드 (훌랄라)  ▲ ㈜유라클 (유라클)  ▲ 트렌차이즈 (오땅비어)  ▲ ㈜퀀텀에너지 (퀀텀에너지)  ▲ 금탑프랜차이즈 (청담동말자싸롱)  ▲ 주식회사 달콤 (달콤커피)  ▲ 신우엔그룹 (미니헌터)  ▲ 아만투어(아만투어)  ▲ 우리동네F&B (경성함바그)  ▲ 하우징콜 (하우징콜)  ▲ 별별치킨 (별별치킨)  ▲ 아코페디코코리아 (아코페디코)  ▲ 리골레토 시카고피자 (리골레토 시카고피자)  ▲ 엉클스 (엉클스)  ▲ 제이지컴퍼니 (정군비어)  ▲ MEXX(금산) (맥스치킨)  ▲ 코잔드침대 (코잔드침대)  ▲ 앤하우스 (앤하우스)  ▲ 본아이에프 (본도시락)  ▲ 에프앤디파트너 (군반장)  ▲ 크라제인터내셔날 (크라제버거)  ▲ 봉화산푸드 (우리동네국수집)  ▲ ㈜지삼백	(어메이징그레이스)  ▲ 강스푸드 (버벅이네)  ▲ 바인인터내셔날 (오드리햅번카페)  ▲ 미코에프씨 (무한사케무사)  ▲ 꿈트리 (꿈트리팡팡)  ▲ 이에이티 (카페잇)  ▲ 82대리운전 (82대리운전)  ▲ 툴인터내셔날 (페이머스팝아트)  ▲ 메탈아이 (펄샤이닝)  ▲ 블랙스톤 (블랙스톤)  ▲ 아스카소 코리아 (탐앤탐스) ▲ 랑코리아	(커피랑도서관) ▲ 칸나커뮤니케이션 (칸나커뮤니케이션) ▲ 비지팅엔젤스코리아 (비지팅엔젤스코리아) ▲ 다이노에프에스 (공룡고기) ▲ 골프프렌드 (골프프렌드) ▲ 정명라인 (에뽕버거) ▲ 잇템 (잇템) ▲ 에버리치에프앤비 (몬스터김밥) ▲ 주식회사 토코 (25카츠) ▲ 제이엠푸드코리아(숙이네조개)  ▲ 1636 소리소리 (한글음성대표전화) ▲ 마이꿈 (마이꿈) ▲ ㈜폴세 (퀸비캔들) ▲ 미래앤에듀케어 (미래앤에듀케어) ▲ ㈜엠떠블유홀딩스 (요요프레시티바) ▲ 와이드컴퍼니 (와이드컴퍼니) ▲ 엔터커피 (엔터커피) ▲ 토즈 스터디센터 (토즈 스터디센터) ▲ 태덕인터내셔날 (대동e렌탈_ ▲ 오리엔트푸드(석관동떡볶이) ▲ 잉켐테크 (잉켐테크) ▲ TS Food&System (파파이스) ▲ 부라더상사㈜ (부라더소잉팩토리) ▲ ㈜베스트그린라이프 (㈜베스트그린라이프) ▲ 올리브 도시락 카페 (올리브 도시락 카페) ▲ (주)오가다	(오쉐이크) ▲ 우리아이엔씨(우리아이엔씨) ▲ 스위트몬스터 (스위트몬스터) ▲ 신라외식개발 (티바두마리치킨) ▲ DT Food (육앤샤) ▲ ㈜해바라기흥업 (해바라기흥업) ▲ ㈜아이비디써클 (헬로피쉬) ▲ 음식점닥터 (수제갈비) ▲ 행운식품 (목촌돼지국밥) ▲ 산쪼메 (산쪼메) ▲ 엔플랫	(엔플랫) ▲ 카카오뻬르떼 (카카오뻬르떼) ▲ ㈜상상에프에스 (김창덕참숯불갈비) ▲ 한울푸드라인 (솔레미오) ▲ 톡테크 (톡테크) ▲ Chili's Grill & Bar (Chili's Grill & Bar) ▲ 오케이포스 (오케이포스) ▲ 쿨사인주식회사 (쿨사인) ▲ 엠디오토 (엠디오토) ▲ 대한보청기 (대한보청기) ▲ 더킹왕짱쪽갈비 (더킹왕짱쪽갈비) ▲ 주식회사 카페두다트 (카페두다트) ▲ 하타가야 (하타가야) ▲ 대우크린존 주식회사 (대우크린존) ▲ 주식회사 와이엔지인더스트리 (그릴드콘) ▲ 카페코나퀸즈 (카페코나퀸즈) ▲ 주식회사 포유비즈 (포유비즈) ▲ 위드인푸드 (걸작떡볶이) ▲ 바로고 (바로고) ▲ 우노스 (우노스) ▲ 오빠가튀긴닭 (오빠가튀긴닭) ▲ (주)크리오션 (크리오션) ▲ 이수고로케 (이수고로케) ▲ ㈜에브릿 (카페빙수와) ▲ 우리창업네트워크 (우리창업네트워크) ▲ 굿모닝컴퓨터 (굿모닝컴퓨터) ▲ 토성에프씨(옛골토성) ▲ ㈜상점설계 (옥루몽) ▲ 주식회사 미듦 (미듦) ▲ 이지이엔티 (비어스탑) ▲ 에그램 (에그램) ▲ 글로벌창업연구소 (글로벌창업연구소)  ▲ 에이셰프요리아카데미 (에이셰프요리아카데미) ▲ 미도엔터프라이즈 (미도엔터프라이즈) ▲ 놀부 (놀부) ▲ 홍대화로초밥 (한끼야끼) ▲ 주식회사 바보스 (바보스) ▲ 바겐 (바겐) ▲ 주식회사오렌지티코리아 (오렌지티) ▲ 프랑스에다녀온붕어빵 (프랑스에다녀온붕어빵) ▲ 창업경제신문(창업경제신문-반찬가게 창업 진이찬방)         강동완 enterfn@mt.co.kr  |   유통생활경제 선임기자입니다.  이 기자의 다른기사 보기 >
             |순천향대 중국유학생, 상하이·화동지역의 한국 중견기업 인턴십 실시  대학경제 권현수 기자 |입력 : 2015.07.31 18:24       기사    소셜댓글(0)   폰트크게 폰트작게           페이스북   트위터 카카오스토리 프린트 E-mail PDF   닫기      Tweet        -->                  이미지 크게보기순천향대학교(총장 서교일)는 지난 6~31일 4주간 상하이에 위치한 한국기업에서 ‘2015 하계 외국인유학생 중국내 한국기업 인턴십 프로그램’을 실시했다고 밝혔다.  이번 인턴십은 중국 국적 8명의 유학생을 대상으로 상하이에 위치한 렛츠고 인터내셔날 등 5개 중국법인에서 4주간 진행했으며, 유학생들의 취업역량강화와 실질적인 취업연계를 겨냥해 운영했다. 특히, 중국 현지에서 수년간 비즈니스를 하고 있는 상하이·화동지역 한국IT기업협의회의 기업인들이 전담 멘토단으로 참여해 학생들을 직접 코칭하고 관리했다.이번 인턴십에 참가한 조맹(曹猛, 국제통상학과 4학년) 학생은 “졸업 후 중국내 한국기업 취업이 목표인데 인턴십 기간동안 실무 경험을 배우고 싶어 지원했다”며, “인턴십을 통해 많이 배워서 원하는 기업에 취업했으면 한다”고 말했다. <!--
             |몬스터 오디오가 지난 3일 세계적인 축구스타 호날두와 함께 제휴해 만든 'ROC' 헤드폰 유튜브 영상을 통해 공개했다. (사진=몬스터 오디오)일명 '호날두 노숙자 몰카'라는 제목으로 제작된 'ROC' 홍보 동영상(youtu.be/68E53lcUIKE)은 호날두가 노숙자 분장을 하고 거리에서 축구공으로 드리블을 하며 시민들에게 관심을 끄는 장면으로 배포 첫날 조회수 200만을 돌파하며 큰 인기를 얻었다.축구스타 호날두의 컨셉을 살린 'ROC' 라인은 오버이어, 블루투스 온이어, 블루투스 이어폰, 블루투스 스피커 등 총 4개의 아이템으로 갖춰졌으며, 가격은 20~40만원대로 다양하다.해당 제품은 8월 하순 예약판매로 진행되며, 예약구매자에 한해 할인혜택 및 사은품 등을 증정할 예정이다.몬스터 오디오 제품을 국내에 수입, 판매하고 있는 플럭스인터내셔날 유한회사 송정규 대표는 "스포츠 마니아들은 누구보다도 자신이 좋아하는 스타에 대한 동경이 강하고, 해당 제품을 통해 자신이 좋아하는 선수를 어필할 수 있다는 점에서 큰 가치를 느끼고 있다. 이에 따라 향후 스포츠 계열 제품을 적극 선보일 예정"이라고 밝혔다.한편 몬스터 사운드 미국 본사는 축구 선수 호날두 외에도 UFC 선수 김동현, 론다 로우지 등 스폰서로 참여하는 등 스포츠 마케팅에 집중하고 있다.신근호 기자 (danielbt@betanews.net)[ IT와 게임 소식, 베타뉴스에서 한방에 해결하세요. www.betanews.net ]
             |외국 대사들을 상대로 파티를 열고 있는 명성황후(오른쪽·신영숙 분)와 고종(박완 분). 올해 20주년을 맞은 뮤지컬 명성황후는 한국 뮤지컬을 대표하는 작품답게 뛰어난 음악, 화려한 무대, 묵직한 메시지와 배우들의 명연으로 관객들을 감동시키고 있다. 사진제공｜에이콤인터내셔날 ■ 20주년 맞은 뮤지컬 ‘명성황후’새로운 넘버 추가·고종의 고뇌 등 리뉴얼신영숙·김법래 열연…또 다른 20년 기대일본인 자객들의 칼이 여인의 몸에 사정없이 꽂힌다. 여인은 고통스러워하며, 관객들을 향해 쓰러진다. 여인의 마지막 몸부림이 멈추자 자객들의 우두머리인 미우라가 그녀를 향해 승자의 발걸음으로 다가간다. 숨이 멎은 여인의 머리끄덩이를 움켜쥐고는 거칠게 치켜 올린다. 관객의 눈에 죽은 여인의 얼굴이 들어온다. 초침이 납덩이처럼 무겁게 흐른다. 재미난 구경거리라도 보여주듯 머리를 들어올리고 있던 미우라가 툭 떨구고는 한 마디 한다. “태워라.” 뮤지컬 명성황후는 무겁다. 두 팔로는 어림이 없어 등에 지어도 보고, 머리에 이어도 보지만 무겁다. 명성황후의 저 유명한 마지막 넘버 ‘백성들아 일어나라’에서조차 꾹꾹 참았던 눈물이 막이 내려간 뒤에야 펑 터지고 말았다. 자리에서 일어나 몸을 돌리는 순간 4층까지 꽉 들어찬 관객들의 모습이 눈에 들어왔던 것이다. 2000여 석을 가득 메운 관객들은 모두 ‘일어나’ 있었다. 젠장, 우리 모두 한 마음, 한 생각이었던 거다. 그랬다. 우리들이 이렇게 일어설 수 있는 한, 이 민족과 이 나라는 너희들에게 지지 않을 것이다. 잊지 않을 것이다. 9월10일까지 서울 서초동 예술의전당 오페라극장에서 공연하는 뮤지컬 명성황후는 올해 20주년을 맞았다. 1995년 명성황후 시해 100주기를 기념해 한국 공연계의 거장이자 풍운아인 윤호진이 10년의 도전과 노력으로 빚어낸 결과물이 명성황후였다. 20년 전, 한국 공연계는 어떻게 이런 대작을 만들 수 있었을까. 이런 완성도 높은 음악을, 감동적인 스토리를, 두 개의 층으로 나뉘는 어마어마한 스케일의 무대를 어떻게 만들고 구현할 수 있었을까. 명성황후 덕에 우리들은 지난 20년간 ‘오페라의 유령’, ‘노트르담 드 파리’, ‘레미제라블’ 앞에서도 당당히 어깨를 펼 수 있었다.● 새로워진 명성황후…신영숙·김법래 연기에 소름이 쫙 20주년 공연을 맞아 제작사인 에이콤인터내셔날은 단단히 칼을 갈고 나왔다. ‘마누라와 자식 빼고는 다 바꾼다’는 의욕이 곳곳에서 느껴졌다. 음악적으로는 새로운 넘버를 추가하거나 편곡했다. 이해하기 어려웠던 용어들을 쉽게 바꿨고 극의 긴장감을 높이기 위해 장면들의 순서와 시간을 재배열했다. 명성황후를 지키는 호위무사 홍계훈의 비중을 높여 애틋한 로맨스를 살렸다. 우유부단하기만 했던 고종은 고뇌하는 대한제국의 황제로 거듭났다. 여기에 배우들. ‘영원한 명성황후’ 이태원의 뒤를 이은 신영숙의 연기는 완벽했다. 신영숙이 그은 선은 너무나도 정확하고 뚜렷해 보탤 것도 뺄 것도 없었다. 특히 2막에서 쏟아내는 에너지는 관객의 허리를 의자 등에 바짝 붙이게 만들 정도로 박력이 있었다. 신영숙 못지않은 대어는 미우라 역의 김법래였다. 명성황후 시해작전인 ‘여우사냥’을 이끄는 미우라. 김법래의 미우라가 뿜어내는 독기는 숨을 쉬기 힘들 정도로 강했다. 정의와 도리, 인간의 숭고함과 희생을 무자비하게 짓밟는 ‘힘’. 그 힘의 숭상과 정당화에 대한 미우라의 논리는 반감에 눈을 부라리면서도 어쩔 수 없이 수긍하게 만드는 ‘힘’이 있었다. 20주년을 맞은 명성황후는 ‘또 다른 20년’이라는 부제를 달았다. 이 자랑스러운 작품을 오래오래 볼 수 있었으면 좋겠다. 조선의, 대한민국의 온 국민이 ‘일어나는’ 그 날까지, 명성황후를 보게 되었으면 좋겠다.양형모 기자 ranbi@donga.com
             |아이는 아쿠아리움에서, 부모는 롯데몰에서 따로 또 같이! 롯데월드 아쿠아리움, '키즈 아쿠아 돌봄 투어' 진행…부모에겐 2시간의 꿀 같은 휴가 제공  머니투데이 김유경 기자 |입력 : 2015.07.30 11:28       기사    소셜댓글(0)   폰트크게 폰트작게           페이스북   트위터 카카오스토리 프린트 E-mail PDF   닫기      Tweet        -->                  이미지 크게보기키즈 아쿠아 돌봄 투어 /사진제공=롯데월드 아쿠아리움이미지 크게보기키즈 아쿠아 돌봄 투어 /사진제공=롯데월드 아쿠아리움미취학 아이를 돌보느라 영화 한편 제대로 보지 못했던 부모들이 서울에서 2시간의 여유를 만끽할 수 있게 됐다.롯데월드 아쿠아리움이 8월 말까지 아이와 부모 모두에게 즐겁고 행복한 시간을 제공하는 '키즈 아쿠아 돌봄 투어'를 진행한다. 48개월 이상 아이부터 미취학 아동까지 부모를 대신해 2시간 동안 아이들을 돌봐주는 프로그램이다. 아이는 아쿠아리움을 신나게 즐기고, 부모는 안심하고 롯데월드몰에서 영화 관람이나 쇼핑 등 오붓한 데이트를 즐길 수 있다. 잠실 롯데월드몰이 아쿠아리움 뿐만 아니라 영화관, 쇼핑몰, 면세점 등이 한 곳에 모여 있는 복합 콤플렉스라 가능하다.특히 '키즈 아쿠아 돌봄 투어'는 유아, 유아미술 전공의 아쿠아 선생님 1명이 3명의 아이를 맡아서 돌보기 때문에 더 안전하고 교육효과까지 기대할 수 있다. '키즈 아쿠아 돌봄 투어'는 생태설명회를 포함한 아쿠아리움 관람은 기본이고, 아이들에게 중요한 3가지를 쑥쑥 키워준다. 잉어젖병과 먹이캡슐로 물고기에게 먹이를 주며 '감성'을 키워주고, 해양생물을 직접 만져보며 '촉감'을 키워주고, 마지막으로 벨루가 등 캐릭터 DIY 만들기를 통해 '상상력'까지 키워준다. 아이들이 이렇게 알차게 시간을 보내는 동안 참여 아이의 부모에게는 메시지를 이용해 총 5장의 사진을 실시간으로 전송해준다. 아이의 안전을 염려하는 부모를 위한 배려 서비스다. 전화와 홈페이지에서 선착순으로 모집하며, 잔여분은 현장 구매할 수 있다. 이 투어의 비용은 한 아이 당 3만원이며 입장료는 별도다. 문의 : (02)3213-3213롯데월드 아쿠아리움은 여름방학 프로그램 '에듀 바캉스'도 진행 중이다. 8월23일까지 운영하는 이번 프로그램들은 국내 아쿠아리움 최초로 별도의 교육장을 마련한 만큼 아이들이 쉽고 즐겁게 해양생물을 접하고 이해할 수 있도록 다채롭게 마련했다. 해양생물에 대한 심화학습 프로그램 '아쿠아리움 마스터', 탐험활동 미션수행을 통해 진행되는 '아쿠아리움 탐험대장', 현장신청을 통해 물고기 티셔츠, 바다비누 등을 제작하는 '방학숙제 존' 등 다양한 프로그램에 참여할 수 있다. '방학숙제 존'은 8월 30일까지 운영한다. 알뜰한 나들이를 위한 지원도 있다. 롯데카드 소지자라면 올해 말까지 실적과 관계없이 본인은 물론 동반 1인까지 30% 우대 혜택을 받을 수 있다. 삼성카드 회원은 8월 31일까지 입장권의 50%를 포인트로 결제할 수 있다. 이미지 크게보기방학숙제 존 /사진제공=롯데월드 아쿠아리움 <!--"""
    val result = Analyzer.parse(text)
    result.foreach(println)
    assert("입력" == result.head.morpheme.surface)
    assert("-" == result.last.morpheme.surface)
  }

  def getSurfacePos(termNode: LNode): String = {
    println(termNode)
    s"${termNode.morpheme.surface}:${termNode.morpheme.feature.head}"
  }
}
