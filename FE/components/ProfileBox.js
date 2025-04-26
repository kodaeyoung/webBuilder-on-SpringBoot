import { useState, useEffect } from "react";
import { useRouter } from "next/router";
import Image from "next/image";
import styles from "./ProfileBox.module.css";

export default function ProfileBox() {

  const [jwt, setJwt] = useState(null);

  const [profileData, setProfileData] = useState({
    name: "",
    picture: "",
  });
  const router = useRouter();

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
        const json = await response.json();
        const data = json.data;

        if (response.ok) {
          setProfileData({
            name: data.name,
            picture: data.picture,
          });
        } else {
          console.error("Failed to fetch profile data");
        }
      } catch (error) {
        console.error("Error fetching profile data:", error);
      }
    };

    fetchProfileData();
  }, [jwt]);

  const handleLogout = async () => {
    try {
      const response = await fetch("http://localhost:8080/logout", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
      });

      if (response.ok) {
        console.log("로그아웃 성공");
        router.push("/");
      } else {
        console.error("로그아웃 실패");
      }
    } catch (error) {
      console.error("로그아웃 중 오류 발생:", error);
    }
  };

  return (
    <>
      <div className={styles.wrap}>
        <h1 className={styles.title}>회원 정보</h1>
        <div className={styles.listWrap}>
          <div className={styles.list}>
            <div className={styles.profileImgWrap}>
              {profileData.picture && (
                <Image
                  src={profileData.picture}
                  alt="Profile Image"
                  className={styles.profileImg}
                  layout="fill"
                  objectFit="cover"
                />
              )}
            </div>
            <div className={styles.profileInfoWrap}>
              <div className={styles.profileName}>
                {profileData.name}
              </div>
            </div>
          </div>
          <div className={styles.list} onClick={handleLogout}>
            로그아웃
          </div>
        </div>
      </div>
    </>
  );
}
