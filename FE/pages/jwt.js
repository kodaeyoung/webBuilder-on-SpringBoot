import { useEffect } from "react";
import { useRouter } from "next/router";

export default function OAuth2RedirectPage() {
  const router = useRouter();

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("token");
    if (token) {
      localStorage.setItem("jwt", token);
      // 원하는 페이지로 이동
      router.replace("/");
    } else {
      router.replace("/login"); // 실패 시 다시 로그인 페이지로
    }
  }, []);

}