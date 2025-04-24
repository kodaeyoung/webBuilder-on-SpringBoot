import { useState, useReducer, useEffect } from "react";
import { useRouter } from "next/router";
import styles from "./Dash.module.css";
import { FaEllipsisV, FaHeart, FaSearch, FaPlus } from "react-icons/fa";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faSpinner,
  faRotate,
  faShareFromSquare,
} from "@fortawesome/free-solid-svg-icons";

import Image from "next/image";
import Btn from "./Btn";
import Modal from "react-modal";
import { SkeletonDash } from "./Skeleton";
import { Tooltip } from "react-tooltip";

// 초기 상태 정의
const initialState = {
  projects: [],
  templates:[],
  sortOrder: "최신순",
  searchQuery: "",
  isDeployModalOpen: false,
  deployName: "",
  showDeployed: false,
  showShared: false,
  dropdownOpen: null,
  isDeleteModalOpen: false,
  isRenameModalOpen: false,
  isShareModalOpen: false,
  selectedProject: null,
  selectedTemplate:null,
  projectName: "",
  category: "",
  templateName:"",
  loading: true,
  dashStructure: [],
  profileImage: "/profile.png",
  displayName: "",
  imageLoading: {},
  deployLoading: false,
  noDifferences: {},
};


// 리듀서 함수 정의
function reducer(state, action) {
  switch (action.type) {
    case "SET_PROJECTS":
      return { ...state, projects: action.payload };
    case "SET_TEMPLATES":
      return { ...state, templates: action.payload };
    case "SET_SORT_ORDER":
      return { ...state, sortOrder: action.payload };
    case "SET_SEARCH_QUERY":
      return { ...state, searchQuery: action.payload };
    case "TOGGLE_DEPLOY_MODAL":
      return { ...state, isDeployModalOpen: !state.isDeployModalOpen };
    case "SET_DEPLOY_NAME":
      return { ...state, deployName: action.payload };
    case "TOGGLE_SHOW_DEPLOYED":
      return { ...state, showDeployed: !state.showDeployed };
    case "TOGGLE_SHOW_SHARED":
      return { ...state, showShared: !state.showShared };
    case "SET_DROPDOWN_OPEN":
      return { ...state, dropdownOpen: action.payload };
    case "TOGGLE_DELETE_MODAL":
      return { ...state, isDeleteModalOpen: !state.isDeleteModalOpen };
    case "TOGGLE_RENAME_MODAL":
      return { ...state, isRenameModalOpen: !state.isRenameModalOpen };
    case "TOGGLE_SHARE_MODAL":
      return { ...state, isShareModalOpen: !state.isShareModalOpen };
    case "SET_SELECTED_PROJECT":
      return { ...state, selectedProject: action.payload };
    case "SET_SELECTED_TEMPLATE":
    return { ...state, selectedTemplate: action.payload };
    case "SET_PAGE_NAME":
      return { ...state, projectName: action.payload };
    case "SET_CATEGORY":
      return { ...state, category: action.payload };
    case "SET_TEMPLATENAME":
      return { ...state, templateName: action.payload };
    case "SET_LOADING":
      return { ...state, loading: action.payload };
    case "SET_PROFILE_IMAGE":
      return { ...state, profileImage: action.payload };
    case "SET_DISPLAY_NAME":
      return { ...state, displayName: action.payload };
    case "SET_IMAGE_LOADING":
      return { ...state, imageLoading: action.payload };
    case "SET_DEPLOY_LOADING":
      return { ...state, deployLoading: action.payload };
    case "SET_DASH_STRUCTURE":
      return { ...state, dashStructure: action.payload };
    case "SET_NO_DIFFERENCES":
      return {
        ...state,
        noDifferences: {
          ...state.noDifferences,
          ...action.payload,
        },
      };
    default:
      return state;
  }
}

const DropdownMenu = ({
  isDeployed, //배포 상태
  onEdit, // 편집 이동
  onDelete, // 삭제
  onDeploy, // 배포하기
  onUndeploy, // 배포 중지
  onShare, //공유하기
  onStopSharing, // 템플릿 공유 중지
  onRename, // 이름 변경
  project, // 선택한 프로젝트 정보
}) => {
  const deploymentLink = `http://localhost:8080/${project.deployDomain}`;

  return (
    <div className={styles.dropdownMenu}>
      {/* 배포 상태에 따라 */}
      {isDeployed ? (
        <>
          <button onClick={onUndeploy}>배포 중지</button>
          <button onClick={() => window.open(deploymentLink, "_blank")}>
            배포 링크 공유
          </button>
        </>
      ) : (
        <button onClick={onShare}>템플릿으로 공유</button>
      )}
      <button onClick={() => onEdit(project)}>프로젝트 편집</button>
      <button onClick={onDelete}>프로젝트 삭제</button>
      <button onClick={onRename}>이름 변경</button>
    </div>
  );
};

export default function Dash() {
  const router = useRouter();
  const [jwt, setJwt] = useState(null);
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    const storedToken = localStorage.getItem("jwt");
    setJwt(storedToken);
  }, []);

  useEffect(() => {
    if (!jwt) return;
    const fetchProfileData = async () => {
      try {
        const response = await fetch("http://localhost:8080/profile", {
          headers: {
            Authorization: `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
        });
        const data = await response.json();

        if (response.ok) {
          dispatch({
            type: "SET_PROFILE_IMAGE",
            payload: data.profileImageUrl || "/profile.png",
          });
          dispatch({
            type: "SET_DISPLAY_NAME",
            payload: data.displayName || "사용자",
          });
        }
      } catch (error) {
        console.error("Error fetching profile data:", error);
      }
    };

    fetchProfileData();
  }, [jwt]);

  useEffect(() => {
    if (!jwt) return;
    fetchAllProjects(); //마운트 시 한 번 실행
  }, [jwt]);

  // 모든 프로젝트 가져오기
const fetchAllProjects = async () => {
  if (!jwt) return;
  try {
    const res = await fetch("http://localhost:8080/dashboard/my-dashboard", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${jwt}`,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }

    const data = await res.json();

    const noDifferences = {};
    data.forEach((project) => {
      noDifferences[project.id] = !project.modified;
    });

    dispatch({ type: "SET_PROJECTS", payload: data });
    dispatch({ type: "SET_NO_DIFFERENCES", payload: noDifferences });
    dispatch({
      type: "SET_DASH_STRUCTURE",
      payload: new Array(data.length).fill(null),
    });
    dispatch({ type: "SET_LOADING", payload: false });
  } catch (error) {
    console.error("Failed to fetch all projects:", error);
    dispatch({ type: "SET_LOADING", payload: false });
  }
};

// 공유한 템플릿 가져오기
const fetchSharedTemplates = async () => {
  if (!jwt) return;
  try {
    const res = await fetch("http://localhost:8080/sharedTemplate/get-mine", {
      headers: {
        Authorization: `Bearer ${jwt}`,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) {
      throw new Error(`HTTP error! status: ${res.status}`);
    }

    // HTTP 상태 코드가 204 (NO_CONTENT)인 경우 빈 배열 처리
    const data = res.status === 204 ? [] : await res.json();

    dispatch({ type: "SET_TEMPLATES", payload: data });
    dispatch({
      type: "SET_DASH_STRUCTURE",
      payload: new Array(data.length).fill(null),
    });
    dispatch({ type: "SET_LOADING", payload: false });
  } catch (error) {
    console.error("Failed to fetch shared templates:", error);
    alert("공유된 템플릿 불러오기 실패");
  }
};

  const checkDifferencesForTemplate = async (projectId) => {
    try {
      const res = await fetch("http://localhost:8080/update-deploy", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ id: projectId }),
      });

      if (!res.ok) {
        const errorMessage = await res.text();
        if (errorMessage.includes("No differences found")) {
          return true;
        }
      }

      return false;
    } catch (error) {
      console.error(
        `Error checking differences for project ${projectId}:`,
        error
      );
      return false;
    }
  };

  const filteredTemplates = state.projects
    .filter((project) =>
      project.projectName
        .toLowerCase()
        .includes(state.searchQuery.toLowerCase())
    )
    .filter((project) => (state.showDeployed ? project.publish : true))

  const sortedTemplates = filteredTemplates.sort((a, b) => {
    if (state.sortOrder === "최신순") {
      return new Date(b.date) - new Date(a.date);
    } 
  });

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {  // 날짜가 유효하지 않으면
        return '';  // 원하는 기본값을 반환하거나 처리할 방법 설정
    }
    return date.toISOString().split("T")[0];
  }

  const openDeployModal = (project) => {
    dispatch({ type: "SET_SELECTED_PROJECT", payload: project });
    dispatch({ type: "TOGGLE_DEPLOY_MODAL" });
  };

  const closeDeployModal = () => {
    dispatch({ type: "TOGGLE_DEPLOY_MODAL" });
  };

// 프로젝트 배포
  const handleDeployProject = async () => {
    if (!state.deployName.trim()) {
      alert("배포할 이름을 입력하세요.");
      return;
    }

    dispatch({ type: "SET_DEPLOY_LOADING", payload: true });

    try {
      const payload = {
        deployName: state.deployName,
        id: state.selectedProject.id,
      };

      const res = await fetch("http://localhost:8080/deploy", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }


      dispatch({
        type: "SET_PROJECTS",
        payload: state.projects.map((t) =>
          t.id === state.selectedProject.id
            ? { ...t, publish: true, deployName: payload.deployName }
            : t
        ),
      });

      alert("배포가 성공적으로 완료되었습니다.");
      closeDeployModal();
    } catch (error) {
      console.error("Failed to deploy project:", error);
      alert("배포에 실패했습니다.");
    } finally {
      dispatch({ type: "SET_DEPLOY_LOADING", payload: false });
    }
  };

  //프로젝트 배포 중지
  const handleUndeployProject = async (projectId) => {
    try {
      const payload = { id: projectId };

      console.log("Payload:", payload);

      const res = await fetch("http://localhost:8080/undeploy", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      dispatch({
        type: "SET_PROJECTS",
        payload: state.projects.map((project) =>
          project.id === projectId
            ? { ...project, publish: false }
            : project
        ),
      });

      alert("배포가 중지되었습니다.");
    } catch (error) {
      console.error("Failed to undeploy project:", error);
      alert("배포 중지에 실패했습니다.");
    }
  };

  // 배포 업데이트
  const handleUpdateProject = async (projectId) => {
    try {
      const res = await fetch("http://localhost:8080/deploy/update", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ id: projectId }),
      });

      if (!res.ok) {
        const errorMessage = await res.text();

        if (errorMessage.includes("No differences found")) {
          dispatch({
            type: "SET_NO_DIFFERENCES",
            payload: { [projectId]: true },
          });
          alert("배포할 수정 내용이 없습니다.");
          return;
        }

        throw new Error(`HTTP error! status: ${res.status}`);
      }

      dispatch({
        type: "SET_NO_DIFFERENCES",
        payload: { [projectId]: true },
      });
      alert("배포가 업데이트 되었습니다.");
    } catch (error) {
      console.error("Failed to update deploy project:", error);
      if (error.message.includes("No differences found")) {
        alert("배포할 수정 내용이 없습니다.");
      } else {
        alert("배포 업데이트에 실패했습니다.");
      }
    }
  };

// 수정
  const handleEditProject = (project) => {
    console.log("프로젝트 경로:", project.projectPath);
    router.push({
      pathname: "/gen",
      query: { projectPath: project.projectPath },
    });
  };

  // 프로젝트 이름 변경
  const handleRenameProject = async () => {
    if (!state.projectName.trim()) {
      console.log("Tlqkf"+state.projectName);
      alert("새로운 이름을 입력하세요.");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/dashboard/${state.selectedProject.id}/update-name?newName=${encodeURIComponent(state.projectName)}`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      const updatedTemplate = await res.json();

      dispatch({
        type: "SET_PROJECTS",
        payload: state.projects.map((project) =>
          project.id === updatedTemplate.id
            ? { ...project, projectName: updatedTemplate.projectName }
            : project
        ),
      });

      console.log("Template renamed successfully:", updatedTemplate);
      closeRenameModal();
    } catch (error) {
      console.error("Failed to rename project:", error);
      alert("이름 변경에 실패했습니다.");
    }
  };

  // 프로젝트 삭제
  const handleDeleteProject = async () => {
    try {
      if (state.selectedProject.publish) {
        await handleUndeployProject(state.selectedProject.id);
      }

      const res = await fetch(
        `http://localhost:8080/dashboard/${state.selectedProject.id}/remove`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      dispatch({
        type: "SET_PROJECTS",
        payload: state.projects.filter(
          (project) => project.id !== state.selectedProject.id
        ),
      });

      console.log("Template deleted successfully:", state.selectedProject);
      closeDeleteModal();
    } catch (error) {
      console.error("Failed to delete project:", error);
      alert("프로젝트 삭제에 실패했습니다.");
    }
  };

  const handleShareTemplate = async () => {
    if (!state.category.trim()) {
      alert("카테고리를 입력해주세요.");
      return;
    }
    if (!state.templateName.trim()) {
      alert("이름을 입력해주세요.");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/dashboard/share`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            selectedProjectId:state.selectedProject.id,
            templateName: state.templateName,
            category: state.category,
          }),
        }
      );

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

      const sharedTemplate = await res.json();

      console.log("Template shared successfully:", sharedTemplate);
      closeShareModal();
      router.push("/temp");
    } catch (error) {
      console.error("Failed to share project:", error);
      alert("템플릿 공유에 실패했습니다.");
      dispatch({ type: "TOGGLE_SHARE_MODAL" });
    }
  };

  // 템플릿 공유 중지
  const handleStopSharingTemplate = async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/sharedTemplate/${state.selectedTemplate.id}/remove`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${jwt}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }

          // 템플릿을 상태에서 제거하거나 업데이트
    dispatch({
      type: "SET_TEMPLATES",
      payload: state.templates.filter(
        (template) => template.id !== state.selectedTemplate.id
      ),
    });

      console.log("Template sharing stopped successfully:", state.selectedTemplate.id);
      closeDeleteModal();

    } catch (error) {
      console.error("Failed to stop sharing template:", error);
      alert("템플릿 공유 중지에 실패했습니다.");
    }
  };


  // 모달


  const openRenameModal = (project) => {
    dispatch({ type: "SET_SELECTED_PROJECT", payload: project });
    dispatch({ type: "TOGGLE_RENAME_MODAL" });
  };

  const closeRenameModal = () => {
    dispatch({ type: "TOGGLE_RENAME_MODAL" });
  };

  const openDeleteModal = (project) => {
    dispatch({ type: "SET_SELECTED_PROJECT", payload: project });
    dispatch({ type: "TOGGLE_DELETE_MODAL" });
  };

  const openStopSharingModal = (template) => {
    dispatch({ type: "SET_SELECTED_TEMPLATE", payload: template });
    dispatch({ type: "TOGGLE_DELETE_MODAL" });
  };

  const closeDeleteModal = () => {
    dispatch({ type: "TOGGLE_DELETE_MODAL" });
  };

  const openShareModal = (project) => {
    dispatch({ type: "SET_SELECTED_PROJECT", payload: project });
    dispatch({ type: "TOGGLE_SHARE_MODAL" });
  };

  const closeShareModal = () => {
    dispatch({ type: "TOGGLE_SHARE_MODAL" });
  };


  const getProfileImageUrl = (url) => {
    return url && url !== "null" ? url : "/profile.png";
  };

  const toggleDropdown = (id) => {
    dispatch({
      type: "SET_DROPDOWN_OPEN",
      payload: state.dropdownOpen === id ? null : id,
    });
  };

  const customStyles = {
    overlay: {
      backgroundColor: "rgba(0, 0, 0, 0.5)",
      zIndex: 1000,
    },
    content: {
      width: "24rem",
      height: "max-content",
      margin: "auto",
      borderRadius: "1rem",
      boxShadow: "0 2px 4px rgba(0, 0, 0, 0.2)",
      padding: "2rem",
      zIndex: 1001,
    },
  };

  return (
    <>
      <Modal
        isOpen={state.isDeleteModalOpen}
        onRequestClose={closeDeleteModal}
        style={customStyles}
      >
        <h1>정말로 삭제하겠습니까?</h1>
        <p>
          {state.showShared
            ? "삭제 시 공유 목록에서 사라집니다."
            : "이 작업은 되돌릴 수 없습니다."}
        </p>
        <div className={styles.modalButtons}>
          <button
            onClick={state.showShared ? handleStopSharingTemplate : handleDeleteProject}
            className={styles.confirmButton}
          >
            예
          </button>
          <button onClick={closeDeleteModal} className={styles.cancelButton}>
            아니요
          </button>
        </div>
      </Modal>

      <Modal
        isOpen={state.isRenameModalOpen}
        onRequestClose={closeRenameModal}
        style={customStyles}
      >
        <h1>이름을 변경하시겠습니까?</h1>
        <input
          className={styles.pageinputform}
          type="text"
          placeholder="이름 입력.."
          onChange={(e) =>
            dispatch({ type: "SET_PAGE_NAME", payload: e.target.value })
          }
        />
        <div className={styles.modalButtons}>
          <button
            onClick={handleRenameProject}
            className={styles.confirmButton}
          >
            예
          </button>
          <button onClick={closeRenameModal} className={styles.cancelButton}>
            아니요
          </button>
        </div>
      </Modal>

      <Modal
        isOpen={state.isShareModalOpen}
        onRequestClose={closeShareModal}
        style={customStyles}
      >
        <h1>템플릿 공유</h1>
        <input
          className={styles.pageinputform}
          type="text"
          placeholder="카테고리 입력.."
          value={state.category}
          onChange={(e) =>
            dispatch({ type: "SET_CATEGORY", payload: e.target.value })
          }
        />
        <input
          className={styles.pageinputform}
          type="text"
          placeholder="템플릿 이름 입력.."
          value={state.templateName}
          onChange={(e) =>
            dispatch({ type: "SET_TEMPLATENAME", payload: e.target.value })
          }
        />
        <div className={styles.modalButtons}>
          <button
            onClick={handleShareTemplate}
            className={styles.confirmButton}
          >
            확인
          </button>
          <button onClick={closeShareModal} className={styles.cancelButton}>
            취소
          </button>
        </div>
      </Modal>

      <Modal
        isOpen={state.isDeployModalOpen}
        onRequestClose={closeDeployModal}
        style={customStyles}
      >
        <h1>배포할 이름을 입력하세요</h1>
        <input
          className={styles.pageinputform}
          type="text"
          placeholder="배포 이름 입력.."
          value={state.deployName}
          onChange={(e) =>
            dispatch({ type: "SET_DEPLOY_NAME", payload: e.target.value })
          }
        />
        <div className={styles.modalButtons}>
          <button
            onClick={handleDeployProject}
            className={styles.confirmButton}
            disabled={state.deployLoading}
          >
            {state.deployLoading ? (
              <FontAwesomeIcon icon={faSpinner} spin />
            ) : (
              "확인"
            )}
          </button>
          <button onClick={closeDeployModal} className={styles.cancelButton}>
            취소
          </button>
        </div>
      </Modal>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>대시보드</h2>
        </div>
        <div className={styles.sectionControls}>
          <div className={styles.sectionLeft}>
            <Btn
              text={"최신순"}
              background={state.sortOrder === "최신순" ? "#4629F2" : "#fff"}
              border={"#4629F2"}
              textColor={state.sortOrder === "최신순" ? "#fff" : "#4629F2"}
              onClick={() =>
                dispatch({ type: "SET_SORT_ORDER", payload: "최신순" })
              }
            />
            <div className={styles.switchContainer}>
              <label className={styles.switchLabel}>
                {state.showDeployed ? "배포 완료" : "배포: 모든 상태"}
              </label>
              <label className={styles.switch}>
                <input
                  type="checkbox"
                  checked={state.showDeployed}
                  onChange={() => dispatch({ type: "TOGGLE_SHOW_DEPLOYED" })}
                />
                <span className={styles.slider}></span>
              </label>
            </div>
            <div className={styles.switchContainer}>
              <label className={styles.switchLabel}>
                {state.showShared ? "템플릿으로 공유중" : "공유: 모든 상태"}
              </label>
              <label className={styles.switch}>
                <input
                  type="checkbox"
                  checked={state.showShared}
                  onChange={async () => {
                    const willBeChecked = !state.showShared;
                
                    dispatch({ type: "TOGGLE_SHOW_SHARED" });
                
                    if (willBeChecked) {
                      await fetchSharedTemplates();
                    } else {
                      await fetchAllProjects();
                    }
                  }}
                />
                <span className={styles.slider}></span>
              </label>
            </div>
          </div>
          <div className={styles.sectionRight}>
            <div className={styles.searchWrap}>
              <div className={styles.searchBox}>
                <FaSearch className={styles.searchIcon} />
                <input
                  type="text"
                  className={styles.searchInput}
                  placeholder="검색어를 입력하세요 ..."
                  value={state.searchQuery}
                  onChange={(e) =>
                    dispatch({
                      type: "SET_SEARCH_QUERY",
                      payload: e.target.value,
                    })
                  }
                />
              </div>
            </div>
          </div>
        </div>
        {state.loading ? (
          <SkeletonDash dashStructure={state.dashStructure} />
        ) : (
          <div className={styles.grid}>
            {sortedTemplates.length === 0 ? (
              <>
                <Btn
                  icon={<FaPlus className={styles.likeIcon} />}
                  width={"14rem"}
                  text={"지금 웹페이지 생성하기!"}
                  background={"#000"}
                  border={"#000"}
                  textColor={"#fff"}
                  onClick={() => router.push("/")}
                />
              </>
            ) : (
              state.showShared ? (
                // 🔽 공유 템플릿 뷰 (템플릿 삭제 버튼만 있음)
                state.templates.map((template) => (
                  <div key={template.id} className={styles.card}>
                    <div className={styles.cardHeader}>
                      <div className={styles.cardProfileWrap}>
                        <div className={styles.cardProfile}>
                          <Image
                            className={styles.cardProfileImg}
                            alt="profile"
                            layout="fill"
                            src={getProfileImageUrl(template.profileImageUrl)}
                          />
                        </div>
                      </div>
                      <div className={styles.cardHeaderInfo}>
                        <div className={styles.cardUser}>
                          {template.userName}
                        </div>
                      </div>
                    </div>
                    <div className={styles.cardImage}>
                      <div className={styles.imageWrapper}>
                        <Image
                          src={`http://app:8080/${template.imagePath}`}
                          alt="Template Screenshot"
                          layout="fill"
                          objectFit="cover"
                        />
                      </div>
                    </div>
                    <div className={styles.cardContent}>
                      <div className={styles.cardTitle}>
                        {template.templateName}
                      </div>
                      <div className={styles.cardSubhead}>
                        {formatDate(template.createdAt)}
                      </div>
                    </div>
                    <div className={styles.cardFooter}>
                      <Btn
                        text={"템플릿 삭제"}
                        background={"#4629F2"}
                        border={"#4629F2"}
                        textColor={"#fff"}
                        width="7rem"
                        onClick={() => openStopSharingModal(template)}
                      />
                    </div>
                  </div>
              ))
            ) : (
              // 대시보드 카드
              sortedTemplates.map((project) => (
                <div key={project.id} className={styles.card}>
                  <div className={styles.cardHeader}>
                    <div className={styles.cardProfileWrap}>
                      <div className={styles.cardProfile}>
                        <Image
                          className={styles.cardProfileImg}
                          alt="profile"
                          layout="fill"
                          src={state.profileImage}
                        />
                      </div>
                    </div>
                    <div className={styles.cardHeaderInfo}>
                      <div className={styles.cardUser}>{state.displayName}</div>
                      <div className={styles.cardShareState}>
                        <div className={styles.cardShareState}>
                          <div
                            className={`${styles.cardShareStateCircle} ${
                              project.publish ? styles.publish : ""
                            }`}
                            data-tooltip-id={`tooltip-${project.id}`}
                            data-tooltip-content={
                              project.publish
                                ? "배포 중입니다."
                                : "배포하고 있지 않습니다."
                            }
                          ></div>
                          <Tooltip
                            id={`tooltip-${project.id}`}
                            place="top"
                            effect="solid"
                          />
                        </div>
                      </div>
                    </div>
                    <div
                      className={styles.cardMenu}
                      style={{ position: "relative" }}
                    >
                      <button
                        className={styles.cardMenuButton}
                        onClick={() => toggleDropdown(project.id)}
                      >
                        <FaEllipsisV />
                      </button>
                      {state.dropdownOpen === project.id && (
                        <DropdownMenu
                          isDeployed={project.publish} //배포 상태
                          onShare={() => openShareModal(project)} //공유하기
                          onDeploy={() => openDeployModal(project)} //배포하기
                          onUndeploy={() => handleUndeployProject(project.id)} // 배포 중지
                          onEdit={handleEditProject} // 편집하기
                          onRename={() => openRenameModal(project)} //이름 변경
                          onDelete={() => openDeleteModal(project)} //삭제하기
                          project={project} //템플릿 데이터
                          noDifferences={state.noDifferences} // Pass this state
                        />
                      )}
                    </div>
                  </div>
                  <div className={styles.cardImage}>
                    <div className={styles.imageWrapper}>
                      {state.imageLoading[project.id] && (
                        <div className={styles.spinnerContainer}>
                          <FontAwesomeIcon
                            icon={faSpinner}
                            spin
                            className={styles.spinner}
                          />
                        </div>
                      )}
                      <Image
                        src={`http://app:8080/${project.imagePath}`}
                        alt="Template Screenshot"
                        layout="fill"
                        objectFit="cover"
                        onLoadingComplete={() =>
                          dispatch({
                            type: "SET_IMAGE_LOADING",
                            payload: {
                              ...state.imageLoading,
                              [project.id]: false,
                            },
                          })
                        }
                      />
                      <div className={styles.cardImageBtn}>
                        <Btn
                          icon={<FontAwesomeIcon icon={faShareFromSquare} />}
                          text={"프로젝트 편집"}
                          background={"#333"}
                          border={"#333"}
                          textColor={"#fff"}
                          width={"7rem"}
                          onClick={() => handleEditProject(project)}
                        />
                      </div>
                    </div>
                  </div>
                  <div className={styles.cardContent}>
                    <div className={styles.cardTitle}>
                      {project.projectName}
                    </div>
                    <div className={styles.cardSubhead}>
                      {formatDate(project.modifiedAt)}
                    </div>
                    <p>{project.content}</p>
                  </div>
                  <div className={styles.cardFooter}>
                    {project.publish ? (
                      <div className={styles.updateDeployBox}>
                        <Btn
                          text={"배포중지"}
                          background={"#c22"}
                          border={"#c22"}
                          textColor={"#fff"}
                          width="7rem"
                          onClick={() => handleUndeployProject(project.id)}
                        />
                        {state.noDifferences[project.id] ? (
                          <Btn
                            disabled={true}
                            text={<FontAwesomeIcon icon={faRotate} />}
                            background={"#999"}
                            border={"#999"}
                            textColor={"#fff"}
                            width="4rem"
                            onClick={() =>
                              console.log("변경할 내용이 없습니다.")
                            }
                          />
                        ) : (
                          <Btn
                            text={<FontAwesomeIcon icon={faRotate} />}
                            background={"#666"}
                            border={"#666"}
                            textColor={"#fff"}
                            width="4rem"
                            onClick={() => handleUpdateProject(project.id)}
                          />
                        )}
                      </div>
                    ) : (
                      <Btn
                        text={
                          state.deployLoading ? (
                            <FontAwesomeIcon icon={faSpinner} spin />
                          ) : (
                            "배포하기"
                          )
                        }
                        background={"#4629F2"}
                        border={"#4629F2"}
                        textColor={"#fff"}
                        width="7rem"
                        onClick={() => openDeployModal(project)}
                        disabled={state.deployLoading}
                      />
                    )}
                  </div>
                </div>
              ))
            )
            )}
          </div>
        )}
      </section>
    </>
  );
}
