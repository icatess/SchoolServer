package com.aura.smartschool.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aura.smartschool.domain.BoardVO;
import com.aura.smartschool.domain.ConsultVO;
import com.aura.smartschool.domain.HomeVO;
import com.aura.smartschool.domain.ManagerVO;
import com.aura.smartschool.domain.MemberVO;
import com.aura.smartschool.domain.NotiVO;
import com.aura.smartschool.domain.OsInfoVO;
import com.aura.smartschool.domain.PayVO;
import com.aura.smartschool.domain.SchoolNotiVO;
import com.aura.smartschool.domain.SchoolVO;
import com.aura.smartschool.domain.SearchVO;
import com.aura.smartschool.domain.SessionVO;
import com.aura.smartschool.domain.TokenVO;
import com.aura.smartschool.result.Result;
import com.aura.smartschool.result.ResultData;
import com.aura.smartschool.result.ResultDataTotal;
import com.aura.smartschool.service.MobileService;
import com.aura.smartschool.util.CommonUtil;
import com.aura.smartschool.util.NetworkUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@RestController
public class AdminController {
	private static Logger logger = LoggerFactory.getLogger(AdminController.class);
	private final String path = "c:\\";
	
	@Autowired
	private MobileService mobileService;

	
	@RequestMapping("/admin/api/authenticate")
	public void getLogin(@RequestBody ManagerVO inManager,  @RequestHeader HttpHeaders headers) {
		logger.debug("/admin/api/authenticate----------------------------------------------------");
		
		String auth = headers.getFirst("X-Auth");
		
		System.out.println("auth:" + auth);
	}
	
	//관리자 페이지 로그인
	@RequestMapping("/api/getLogin")
	public Result getLogin(@RequestBody ManagerVO inManager) {
		logger.debug("/admin/api/getLogin--------------------------------------------------------");
		
		//토큰 생성
		ManagerVO manager = mobileService.getManager(inManager);
		
		if(manager != null && manager.getRole_id() < 3) {
			String token;
			try {
				token = CommonUtil.createJWT(manager.getId(), manager.getId(), String.valueOf(manager.getRole_id()), 600 * 60 * 1000);
				manager.setToken(token);
				return new ResultData<ManagerVO>(0, "success", manager);
			} catch (IOException e) {
				return new ResultData<ManagerVO>(200, "서버오류가 발생하였습니다. 잠시후에 시도하세요.", manager);
			} 
			
		} else {
			return new ResultData<ManagerVO>(100, "아이디나 패스워드를 확인하세요.", manager);
		}
	}
	
	//홈 아이디 리스트 가져오기 (관리자, 페이징)
	@RequestMapping("/admin/api/getHomeList")
	public Result getHomeList(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getHomeList-----------------------------------------------------");
		
		List<HomeVO> homeList = mobileService.selectHomeList(search);
		int total = mobileService.countHomeList(search);
		
		return new ResultDataTotal<List<HomeVO>>(0, "success", homeList, total);
	}
	
	//홈 아이디 변경
	@RequestMapping("/admin/api/modifyHome")
    public Result modifyHome(@RequestBody SearchVO search) {
		logger.debug("/admin/api/modifyHome------------------------------------------------------");
		
		HomeVO home = new HomeVO();
		home.setHome_id(search.getNew_home_id());
		int count = mobileService.countHome(home);
		
		if (count>0) {
			return new Result(100, "동일한 가족명이 존재합니다.");
		} else {
			long resultCount = mobileService.modifyHome(search);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "remove failed");
			}
		}
	}
	
	//홈 아이디 추가
	@RequestMapping("/admin/api/addHome")
    public Result addHome(@RequestBody HomeVO home) {
		logger.debug("/admin/api/addHome------------------------------------------------------------");
		
		int count = mobileService.countHome(home);
		if (count>0) {
			return new Result(100, "동일한 가족명이 존재합니다.");
		} else {
			long resultCount = mobileService.addHome(home);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "remove failed");
			}
		}
	}
	
	//가족 모든 구성원 (삭제포함) 리스트 가져오기
	@RequestMapping("/admin/api/getAllMember")
    public Result getAllMember(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getAllMember----------------------------------------------------");
		
		List<MemberVO> memberList = mobileService.getAllMember(search);
		int total = mobileService.countAllMember(search);
		
		return new ResultDataTotal<List<MemberVO>>(0, "success", memberList, total);
	}
	
	//가족 멤버 등록
	@RequestMapping("/admin/api/addMember")
    public Result addMember(@RequestBody MemberVO member) {
		logger.debug("/admin/api/addMember-------------------------------------------------------");
		
		try {
			if(mobileService.checkMemberExistInHome(member) > 0) {
				return new Result(100, "중복된 전화번호가 존재하거나 가족명내에 동일한 이름이 존재합니다.");
			} else {
				long resultCount = mobileService.insertMember(member);
				if(resultCount > 0) {
					return new Result(0, "success");
				} else {
					return new Result(100, "insert failed");
				}
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	
	//가족 멤버 수정
	@RequestMapping("/admin/api/modifyMember")
    public Result modifyMember(@RequestBody MemberVO member) {
		logger.debug("/admin/api/modifyMember----------------------------------------------------------");
		
		if(mobileService.checkMemberExistInHome(member) > 1) {
			return new Result(100, "중복된 전화번호가 존재하거나 가족명내에 동일한 이름이 존재합니다.");
		} else {
			long resultCount = mobileService.updateMember(member);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "update failed");
			}
		}
	}
    
	//get school list of member
	@RequestMapping("/admin/api/getSchoolListOfMember")
    public ResultData<List<SchoolVO>> getSchoolListOfMember(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getSchoolListOfMember-------------------------------------------");
		List<SchoolVO> schoolList = mobileService.getSchoolListOfMember(search);
		int total = mobileService.countSchoolListOfMember(search);
		
		return new ResultDataTotal<List<SchoolVO>>(0, "success", schoolList, total);
	}
	
	@RequestMapping("/admin/api/modifySchool")
    public Result updateSchool(@RequestBody SchoolVO school) {
		logger.debug("/admin/api/modifySchool----------------------------------------------------");
		
		long resultCount = mobileService.updateSchool(school);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	//앱 공지사항 가져오기=============================================================================
	@RequestMapping("/admin/api/getNotiList")
    public ResultData<List<NotiVO>> getNotiList(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getNotiList--------------------------------------------------");
		List<NotiVO> notiList = mobileService.getNotiList(search);
		int total = mobileService.countNotiList(search);
		
		return new ResultDataTotal<List<NotiVO>>(0, "success", notiList, total);
	}
	
	@RequestMapping("/admin/api/addNoti")
    public Result addNoti(@RequestBody NotiVO inNoti) {
		logger.debug("/admin/api/addNoti-------------------------------------------------------------");
		
		try {
			long resultCount = mobileService.addNoti(inNoti);
			if(resultCount > 0) {
				//send gcm
				List<MemberVO> memberList = mobileService.getAllMemberOfGcm();
				
				JsonObject jsonData = new JsonObject();
				JsonObject value = new JsonObject();
				value.addProperty("title", inNoti.getTitle());
				value.addProperty("content", inNoti.getContent());
				jsonData.addProperty("command", "appNoti");
				jsonData.addProperty("value", value.toString());
				
				/*JsonArray array = new JsonArray(); //get gcm_id
				for(MemberVO m : memberList) {
					if (m.getGcm_id() != null && !"".equals(m.getGcm_id())) {
						array.add(new JsonPrimitive(m.getGcm_id()));
					}
				}*/
				
				//100건 단위로 나눠서 보낸다.
				int loop = memberList.size()/100 + 1;
				for( int i=0; i < loop ; ++i) {
					JsonArray array = new JsonArray();
					for(int k=0; k < 100 ; ++k) {
						if(k+100*i >= memberList.size()) break;
						array.add(new JsonPrimitive(memberList.get(k + 100 * i).getGcm_id()));
					}
					NetworkUtil.requestGCM(array, jsonData);
				}
				
				//NetworkUtil.requestGCM(array, jsonData);
				
				return new Result(0, "success");
			} else {
				return new Result(100, "insert failed");
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	
	@RequestMapping("/admin/api/modifyNoti")
    public Result modifyNoti(@RequestBody NotiVO inNoti) {
		logger.debug("/admin/api/modifyNoti----------------------------------------------------------");
		
		long resultCount = mobileService.modifyNoti(inNoti);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/removeNoti")
    public Result removeNoti(@RequestBody NotiVO inNoti) {
		logger.debug("/admin/api/removeNoti----------------------------------------------------------");
		
		long resultCount = mobileService.removeNoti(inNoti);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "delete failed");
		}
	}
	
	//학교 게시판(QnA) 가져오기==========================================================================
	@RequestMapping("/admin/api/getBoardList")
    public ResultData<List<BoardVO>> getBoardList(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getBoardList--------------------------------------------------");
		List<BoardVO> boardList = mobileService.getBoardList(search);
		int total = mobileService.countBoardList(search);
		
		return new ResultDataTotal<List<BoardVO>>(0, "success", boardList, total);
	}
	
	@RequestMapping("/admin/api/addSchoolNoti")
    public Result addSchoolNoti(HttpServletRequest request, @RequestParam("data") String data, @RequestParam(value="file", required=false) MultipartFile file) {
		logger.debug("/admin/api/addSchoolNoti---------------------------------------------------");
		
		String path = request.getServletContext().getRealPath("/upload");
		Gson gson = new Gson();
		SchoolNotiVO notiVO = gson.fromJson(data, SchoolNotiVO.class);
		
		System.out.println("path:" + path);
		System.out.println("data:" + data);
		System.out.println("data:" + notiVO.getSchool_id());

		//파일 처리
		if (file != null && !file.isEmpty()) {
			//파일 중복 체크
			String filename = file.getOriginalFilename();
			SchoolNotiVO noti = mobileService.getFilenameOfSchoolNoti(filename);
			if(noti != null) {
				return new Result(100, "파일명이 중복입니다.");
			} else {
				notiVO.setFilename(filename);
			}
		
			//파일 저장
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(path + notiVO.getFilename())));
                stream.write(bytes);
                stream.close();
                System.out.println("success: " + path + notiVO.getFilename());
            } catch (Exception e) {
            	System.out.println("You failed to upload ");
            }
        } 
		
		long resultCount = mobileService.addSchoolNoti(notiVO);
		if(resultCount > 0) {
			//send gcm
			SchoolVO school = mobileService.getSchoolById(notiVO.getSchool_id());
			List<MemberVO> memberList = mobileService.selectMemberOfSchool(school);
			JsonArray array = new JsonArray(); //get gcm_id
			for(MemberVO m : memberList) {
				if (m.getGcm_id() != null && !"".equals(m.getGcm_id())) {
					array.add(new JsonPrimitive(m.getGcm_id()));
				}
			}
			
			JsonObject jsonData = new JsonObject();
			JsonObject value = new JsonObject();
			value.addProperty("school_id", school.getSchool_id());
			value.addProperty("school_name", school.getSchool_name());
			value.addProperty("category", notiVO.getCategory());
			value.addProperty("title", notiVO.getTitle());
			value.addProperty("content", notiVO.getContent());
			value.addProperty("noti_date",notiVO.getNoti_date());
			jsonData.addProperty("command", "school");
			jsonData.addProperty("value", value.toString());
			
			NetworkUtil.requestGCM(array, jsonData);

			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/modifySchoolNoti")
    public Result modifySchoolNoti(HttpServletRequest request, @RequestParam("data") String data, @RequestParam(value="file", required=false) MultipartFile file) {
		logger.debug("/admin/api/modifySchoolNoti------------------------------------------------");
		
		String path = request.getServletContext().getRealPath("/upload");
		Gson gson = new Gson();
		SchoolNotiVO notiVO = gson.fromJson(data, SchoolNotiVO.class);
		
		System.out.println("path:" + path);
		System.out.println("data:" + data);
		System.out.println("data:" + notiVO.getSchool_id());

		//파일 처리
		if (file != null && !file.isEmpty()) {
			//파일 중복 체크
			String filename = file.getOriginalFilename();
			SchoolNotiVO noti = mobileService.getFilenameOfSchoolNoti(filename);
			if(noti != null) {
				return new Result(100, "파일명이 중복입니다.");
			} else {
				notiVO.setFilename(filename);
			}
		
			//파일 저장
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(path + notiVO.getFilename())));
                stream.write(bytes);
                stream.close();
                System.out.println("success: " + path + notiVO.getFilename());
            } catch (Exception e) {
            	System.out.println("You failed to upload ");
            }
        } 
		
		long resultCount = mobileService.modifySchoolNoti(notiVO);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/removeSchoolNoti")
    public Result removeSchoolNoti(HttpServletRequest request, @RequestBody SchoolNotiVO inNoti) {
		logger.debug("/admin/api/removeSchoolNoti------------------------------------------------");
		SchoolNotiVO noti = mobileService.getSchoolNoti(inNoti);
		long resultCount = mobileService.removeSchoolNoti(inNoti);
		
		//첨부파일이 있다면 첨부화일도 지우다.
		if(noti.getFilename() != null && !noti.getFilename().isEmpty()) {
			String path = request.getServletContext().getRealPath("/upload");
			File f = new File(path + noti.getFilename());
			if(f != null) {
				f.delete();
			}
		}
		
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/getSchoolNotiList")
    public ResultData<List<SchoolNotiVO>> getSchoolNotiList(@RequestBody SchoolNotiVO noti) {
		logger.debug("/admin/api/getSchoolNotiList-----------------------------------------------");
		List<SchoolNotiVO> notiList = mobileService.getSchoolNotiList(noti);
		int total = mobileService.countSchoolNotiList(noti);
		return new ResultDataTotal<List<SchoolNotiVO>>(0, "success", notiList, total);
	}
	
	
	
	//add session and consult
	@RequestMapping("/admin/api/addConsult")
    public Result addConsult(@RequestBody SessionVO inSession) {
		logger.debug("/admin/api/addConsult----------------------------------------------------");
		SessionVO outSession = mobileService.selectSession(inSession);
		
		int session_id;
		if(outSession == null) {
			mobileService.insertSession(inSession);
			SessionVO session = mobileService.selectLastSession();
			session_id = session.getSession_id();
		} else {
			session_id = outSession.getSession_id();
		}
		ConsultVO consult = new ConsultVO();
		consult.setSession_id(session_id);
		consult.setContent(inSession.getContent());
		consult.setWho(inSession.getWho());
		mobileService.insertConsult(consult);
		
		//if who is 1, send gcm : member_id = > gcm id, content
		if(inSession.getWho() == 1) {
			JsonArray array = new JsonArray(); //get gcm_id
			MemberVO m = new MemberVO();
			m.setMember_id(inSession.getMember_id());
			MemberVO member = mobileService.selectMember(m);
			array.add(new JsonPrimitive(member.getGcm_id()));
			
			JsonObject data = new JsonObject();
			JsonObject value = new JsonObject();
			value.addProperty("category", inSession.getCategory());
			value.addProperty("content", inSession.getContent());
			data.addProperty("command", "consult");
			data.addProperty("value", value.toString());
			
			NetworkUtil.requestGCM(array, data);
		}
		
		return new Result(0, "success");
	}
	
	//get current session
	@RequestMapping("/admin/api/getSessionList")
    public ResultData<List<SessionVO>> getSessionList(@RequestBody SearchVO search) {
		logger.debug("/admin/api/getSessionList--------------------------------------------------");
		List<SessionVO> sessionList = mobileService.selectSessionOngoingList(search);
		int total = mobileService.countSessionOngoingList(search);
		
		return new ResultDataTotal<List<SessionVO>>(0, "success", sessionList, total);
	}
	
	//get consult list
	@RequestMapping("/admin/api/getConsultList")
    public ResultData<List<ConsultVO>> getConsultList(@RequestBody SessionVO inSession) {
		logger.debug("/admin/api/getConsultList--------------------------------------------------");
		List<ConsultVO> consultList = mobileService.selectConsultList(inSession);

		return new ResultData<List<ConsultVO>>(0, "success", consultList);
	}
	
	
	//관리자화면: 사용자 관리=====================================================================
	@RequestMapping("/admin/api/addManager")
    public Result addManager(@RequestBody ManagerVO manager) {
		logger.debug("/api/addManager------------------------------------------------------------");
		
		try {
			long resultCount = mobileService.addManager(manager);
			if(resultCount > 0) {
				return new Result(0, "success");
			} else {
				return new Result(100, "insert failed");
			}
		} catch (PersistenceException e) {
			return new Result(100, "insert failed");
		} 
	}
	
	@RequestMapping("/admin/api/modifyManager")
    public Result modifyManager(@RequestBody ManagerVO manager) {
		logger.debug("/api/modifyManager----------------------------------------------------------");
		
		long resultCount = mobileService.modifyManager(manager);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "update failed");
		}
	}
	
	@RequestMapping("/admin/api/removeManager")
    public Result removeManager(@RequestBody ManagerVO manager) {
		logger.debug("/api/removeManager----------------------------------------------------------");
		
		long resultCount = mobileService.removeManager(manager);
		if(resultCount > 0) {
			return new Result(0, "success");
		} else {
			return new Result(100, "delete failed");
		}
	}
	
	@RequestMapping("/admin/api/getManager")
    public ResultData<ManagerVO> getManager(@RequestBody ManagerVO inManager) {
		logger.debug("/api/getManager--------------------------------------------------");
		ManagerVO manager = mobileService.getManager(inManager);
		
		if(manager == null) {
			return new ResultData<ManagerVO>(100, "fail", manager);
		} else {
			manager.setPass("");
			return new ResultData<ManagerVO>(0, "success", manager);
		}
	}
	
	@RequestMapping("/admin/api/getManagerList")
    public ResultDataTotal<List<ManagerVO>> getManagerList(@RequestBody SearchVO search) {
		logger.debug("/api/getManagerList--------------------------------------------------");
		List<ManagerVO> managerList = mobileService.getManagerList(search);
		
		int total = mobileService.countManager(search);
		
		return new ResultDataTotal<List<ManagerVO>>(0, "success", managerList, total);
	}
	
	//결제관련--------------------------------------------------------------------------------------
	@RequestMapping("/admin/api/getPayList")
    public Result getPayList(@RequestBody MemberVO member) {
		logger.debug("/api/getPayList------------------------------------------------------------");
		
		List<PayVO> payList = mobileService.getPayList(member);
		
		return new ResultData<List<PayVO>>(0, "success", payList);

	}
	
	//결제정보 추가
	@RequestMapping("/admin/api/addPay")
    public Result addPay(@RequestBody PayVO pay) {
		logger.debug("/api/addPay----------------------------------------------------------------");
		
		long result = mobileService.addPay(pay);
		
		return new Result(0, "success");
	}
	
	//결제정보 수정
	@RequestMapping("/admin/api/modifyPay")
    public Result modifyPay(@RequestBody PayVO pay) {
		logger.debug("/api/modifyPay-------------------------------------------------------------");
		
		long result = mobileService.modifyPay(pay);
		
		return new Result(0, "success");
	}
	
	//결제정보 삭제
	@RequestMapping("/admin/api/removePay")
    public Result removePay(@RequestBody PayVO pay) {
		logger.debug("/api/removePay-------------------------------------------------------------");
		
		long result = mobileService.removePay(pay);
		
		return new Result(0, "success");
	}
	
	//os 버전정보 모두 가져오기
	@RequestMapping("/admin/api/getOsInfoList")
    public Result getOsInfoList() {
		logger.debug("/admin/api/getOsInfoList---------------------------------------------------");
		
		List<OsInfoVO> osInfoList = mobileService.getOsInfoList();
		
		return new ResultData<List<OsInfoVO>>(0, "success", osInfoList);
	}
	
	//결제정보 수정
	@RequestMapping("/admin/api/modifyOsInfo")
    public Result modifyOsInfo(@RequestBody OsInfoVO osInfo) {
		logger.debug("/admin/api/modifyOsInfo----------------------------------------------------");
		
		long result = mobileService.modifyOsInfo(osInfo);
		
		return new Result(0, "success");
	}
}
