FROM nginxinc/nginx-unprivileged:alpine

COPY etc/docker/config/nginx/nginx.conf /etc/nginx/conf.d/default.conf

COPY ui/target/dist/mra/browser/ /usr/share/nginx/html/

EXPOSE 8080

ENTRYPOINT ["nginx", "-g", "daemon off;"]