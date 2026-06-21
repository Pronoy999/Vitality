![Build Status](https://github.com/Pronoy999/Vitality/actions/workflows/main.yml/badge.svg)

Docker Command to Run Java API: 
docker run -d \
  --name vitality-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://172.17.0.1:5432/vitality \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=admin \
  pronoytw/vitality:latest


  
# API Docs:
https://documenter.getpostman.com/view/5329722/2sBXirkUC7
