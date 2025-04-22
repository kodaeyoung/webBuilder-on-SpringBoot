/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  webpack: (config, { isServer }) => {
    return config;
  },
  images: {
    //domains: ["localhost", "lh3.googleusercontent.com","localhost:8080"],
    remotePatterns: [
      {
        protocol: 'http',
        hostname: 'localhost',
      },
      {
        protocol: 'https',
        hostname: 'lh3.googleusercontent.com'
      },
      {
        protocol: 'http',
        hostname: 'app'
      }
    ],
  },
};

export default {
  ...nextConfig,
  assetPrefix: "",
};
