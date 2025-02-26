package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherClient {

    private final RestTemplate restTemplate;

    public WeatherClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // 외부 API를 사용하여 오늘의 날씨 데이터를 가져와서 weatherDto에 맞추어 반환하는 메소드
    // 예외 : 1. API 요청자체가 실패한 경우, 2. 날씨 데이터가 빈 경우(null or 0개) 3. 오늘의 날씨 데이터가 없는 경우
    public String getTodayWeather() {

        // buildWeatherApiUri(): 요청할 날씨API URL을 생성하여
        // restTemplate.getForEntity(): 해당 API에게 데이터 요청을 보낸 후
        // WeatherDto[].class: 받은 값을 WeaterDto 배열로 변환하여
        // ResponseEntity<WeatherDto[]> responseEntity 에 할당하기
        ResponseEntity<WeatherDto[]> responseEntity =
                restTemplate.getForEntity(buildWeatherApiUri(), WeatherDto[].class);

        // 위에서 할당받은 값(responseEntity)의 상태코드가 정상OK(200)인지 체크하여 정상이 아니면 ServerException 발생
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
        }

        // 위에서 할당받은 값(responseEntity)의 Body 만을 WeatherDto 배열에 할당하기
        // 위에서 할당받은 값의 상태코드가 정상이긴하지만, 배열이 null이거나 비어있는 것 역시 ServerException 발생
        WeatherDto[] weatherArray = responseEntity.getBody();
        if (weatherArray == null || weatherArray.length == 0) {
            throw new ServerException("날씨 데이터가 없습니다.");
        }

        // [문제점] 37 Line의 WeatherDto[] weatherArray = responseEntity.getBody(); 는
        //        계속 필요하지 않다가 47 Line 인 여기서부터 필요하다, 굳이 저 위에서 선언할 필요가 없다.
        // [해결방안] 위 코드를 이쪽으로 옮김과 동시에 불필요한 else문을 제거하고, 분리한다


        // 값을 정상적으로 받아온 경우, 오늘 날짜(getCurrentDate())를 문자열에 할당
        String today = getCurrentDate();

        // for: API로부터 받아온 값의 body배열 (WeatherDto[] 형)을 하나씩 꺼내어 루프를 돌려서
        // 오늘 날짜에 해당되는 WeatherDto형 값 발견 시, 해당 날짜의 날씨값(weatherDto.getWeather()) 반환
        for (WeatherDto weatherDto : weatherArray) {
            if (today.equals(weatherDto.getDate())) {
                return weatherDto.getWeather();
            }
        }

        // for문을 전부 돌렸음에도 불구하고 날짜에 해당하는 WeatherDto형 값을 발견하지 못한 경우
        // ServerException 예외 발생
        throw new ServerException("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }

    private URI buildWeatherApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://f-api.github.io")
                .path("/f-api/weather.json")
                .encode()
                .build()
                .toUri();
    }

    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return LocalDate.now().format(formatter);
    }
}