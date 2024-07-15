package kr.co.cjdashboard.api.dashboard.service;

import kr.co.cjdashboard.api.dashboard.entity.CjLog;
import kr.co.cjdashboard.api.dashboard.model.*;
import kr.co.cjdashboard.exception.InvalidException;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static kr.co.cjdashboard.api.dashboard.constant.FieldName.*;
import static kr.co.cjdashboard.api.dashboard.service.DateComponent.*;
import static kr.co.cjdashboard.util.MessageUtil.exceptionMessage;

/**
 * -------------------------------------------------------------------------------------
 * ::::::'OO::::'OOO::::'OO:::'OO:'OO::::'OO:'OOOOOOOO:::'OOOOOOO::'OO::::'OO:'OO....OO:
 * :::::: OO:::'OO OO:::. OO:'OO:: OO::::.OO: OO.....OO:'OO.....OO: OO:::: OO: OOO...OO:
 * :::::: OO::'OO:..OO:::. OOOO::: OO::::.OO: OO::::.OO: OO::::.OO: OO:::: OO: OOOO..OO:
 * :::::: OO:'OO:::..OO:::. OO:::: OO::::.OO: OOOOOOOO:: OO::::.OO: OO:::: OO: OO.OO.OO:
 * OO:::: OO: OOOOOOOOO:::: OO:::: OO::::.OO: OO.. OO::: OO::::.OO: OO:::: OO: OO..OOOO:
 * :OO::::OO: OO.....OO:::: OO:::: OO::::.OO: OO::. OO:: OO::::.OO: OO:::: OO: OO:..OOO:
 * ::OOOOOO:: OO:::..OO:::: OO::::. OOOOOOO:: OO:::. OO:. OOOOOOO::. OOOOOOO:: OO::..OO:
 * :......:::..:::::..:::::..::::::.......:::..:::::..:::.......::::.......:::..::::..::
 * <p>
 * packageName    : kr.co.cjdashboard.service
 * fileName       : DashboardService
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    @Value("${top.count}")
    private int topCount;
    @Value("${mode}")
    private String mode;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final CustomerService customerService;
    private final TypeService typeService;

    //전체 등록 현황
    public List<TotalRegistrationStatusDto> getTotalRegistrationStatusGroupByIPAndType() {
        String today = actualToday();
        String yesterday = actualYesterday();
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today))
                        .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE)))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_IP_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE)))
                .build();

        //2. ES query 집계
        List<String> defaultTypes = new ArrayList<>();
        getTypes().forEach(defaultType -> defaultTypes.add(dynamicExtractType(defaultType)));
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayIpTerms = todayFilter.getAggregations().get(TODAY_IP_FILTER);

        Map<String, Long> todayMap = new HashMap<>();
        todayIpTerms.getBuckets()
                .forEach(todayIpBucket -> {
                    Terms todayTypeTerms = todayIpBucket.getAggregations().get(TODAY_TYPE_FILTER);
                    todayTypeTerms.getBuckets().forEach(todayTypeBucket -> {
                        String type = dynamicExtractType(todayTypeBucket.getKeyAsString());
                        todayMap.put(type, todayMap.getOrDefault(type, 0L) + 1);
                    });
                });

        Map<String, Long> yesterdayMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayIpTerms = yesterdayFilter.getAggregations().get(YESTERDAY_IP_FILTER);
        yesterdayIpTerms.getBuckets()
                .forEach(yesterdayIpBucket -> {
                    Terms yesterdayTypeTerms = yesterdayIpBucket.getAggregations().get(YESTERDAY_TYPE_FILTER);
                    yesterdayTypeTerms.getBuckets().forEach(yesterdayTypeBucket -> {
                        String type = dynamicExtractType(yesterdayTypeBucket.getKeyAsString());
                        yesterdayMap.put(type, yesterdayMap.getOrDefault(type, 0L) + 1);
                    });
                });

        //3. 오늘 날짜에 해당 하는 Type이 없을 경우 0으로 초기화
        defaultTypes.forEach(defaultType -> {
            String type = dynamicExtractType(defaultType);
            todayMap.put(type, todayMap.getOrDefault(type, 0L));
            yesterdayMap.put(type, yesterdayMap.getOrDefault(type, 0L));
        });

        List<TotalRegistrationStatusDto> result = new ArrayList<>();
        todayMap.forEach((type, count) -> {
            result.add(new TotalRegistrationStatusDto(type, count, yesterdayMap.get(type)));
        });

        return result;
    }

    // 고객사별 수집 현황
    public List<CollectionStatusByCustomerDto> getCollectionStatusGroupByCustomerAndType() {
        String today = actualToday();
        String yesterday = actualYesterday();
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today))
                        .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                            .subAggregation(AggregationBuilders.terms(TODAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE))))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                            .subAggregation(AggregationBuilders.terms(YESTERDAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE))))
                .build();

        List<Customer> defaultCustomers = getCustomers();
        List<String> defaultTypes = new ArrayList<>();
        getTypes().forEach(defaultType -> defaultTypes.add(dynamicExtractType(defaultType)));
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        //2. ES query 집계
        Map<String, Map<String, Long>> todayStatusCountMap = new HashMap<>();
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayIPTerms = todayFilter.getAggregations().get(TODAY_IP_FILTER);
        todayIPTerms.getBuckets()
                .forEach(todayIPBucket -> {
                    Terms todayCustomerTerms = todayIPBucket.getAggregations().get(TODAY_CUSTOMER_FILTER);
                    todayCustomerTerms.getBuckets().forEach(todayCustomerBucket -> {
                        String customer = todayCustomerBucket.getKeyAsString();
                        Terms todayTypeTerms = todayCustomerBucket.getAggregations().get(TODAY_TYPE_FILTER);
                        todayTypeTerms.getBuckets().forEach(todayTypeBucket -> {
                            String type = dynamicExtractType(todayTypeBucket.getKeyAsString());
                            long count = todayTypeBucket.getDocCount();
//                        todayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
//                                .merge(type, count, Long::sum);
                            todayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
                                    .merge(type, 1L, Long::sum);
                        });
                        defaultTypes.forEach(defaultType ->
                                todayStatusCountMap.get(customer).putIfAbsent(defaultType, 0L));
                    });
                });

        Map<String, Map<String, Long>> yesterdayStatusCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        //4. 어제 날짜 데이터 구하기
        Terms yesterdayIPTerms = yesterdayFilter.getAggregations().get(YESTERDAY_IP_FILTER);
        yesterdayIPTerms.getBuckets().forEach(yesterdayIPBucket -> {
            Terms yesterdayCustomerTerms = yesterdayIPBucket.getAggregations().get(YESTERDAY_CUSTOMER_FILTER);
            yesterdayCustomerTerms.getBuckets().forEach(yesterdayCustomerBucket -> {
                String customer = yesterdayCustomerBucket.getKeyAsString();
                Terms yesterdayTypeTerms = yesterdayCustomerBucket.getAggregations().get(YESTERDAY_TYPE_FILTER);
                yesterdayTypeTerms.getBuckets().forEach(yesterdayTypeBucket -> {
                    String type = dynamicExtractType(yesterdayTypeBucket.getKeyAsString());
                    long count = yesterdayTypeBucket.getDocCount();
//                        yesterdayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
//                                .merge(type, count, Long::sum);
                    yesterdayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
                            .merge(type, 1L, Long::sum);
                });
                defaultTypes.forEach(defaultType ->
                        yesterdayStatusCountMap.get(customer).putIfAbsent(defaultType, 0L));
            });
        });
        //5. 오늘 날짜에 해당 하는 Type이 없을 경우 0으로 초기화
        defaultCustomers.forEach(defaultCustomer -> {
            todayStatusCountMap.putIfAbsent(defaultCustomer.getId(), defaultTypes.stream()
                    .collect(Collectors.toMap(defaultType -> dynamicExtractType(defaultType), count -> 0L, (existing, replacement) -> existing)));
            yesterdayStatusCountMap.putIfAbsent(defaultCustomer.getId(), defaultTypes.stream()
                    .collect(Collectors.toMap(defaultType -> dynamicExtractType(defaultType), count -> 0L, (existing, replacement) -> existing)));
        });

        List<CollectionStatusByCustomerDto> result = new ArrayList<>();
        todayStatusCountMap.forEach((customer, countMap) -> {
            List<CollectionStatusByCustomerDto.CollectionStatusByCustomerDetailDto> dataList = new ArrayList<>();
            countMap.forEach((type, todayCount) -> {
                if(type.equals(LINUX) || type.equals(WINDOWS) || type.equals(AIX) || type.equals(SAN_NET) || type.equals(STO)) {
                    long yesterdayCount = 0;
                    String dynamicType = dynamicExtractType(type);
                    if(yesterdayStatusCountMap.get(customer) != null && yesterdayStatusCountMap.get(customer).get(dynamicType) != null) {
                        yesterdayCount = yesterdayStatusCountMap.get(customer).get(dynamicType);
                    }
                    dataList.add(CollectionStatusByCustomerDto.CollectionStatusByCustomerDetailDto.builder()
                            .type(dynamicType)
                            .todayCount(todayCount)
                            .yesterdayCount(yesterdayCount)
                            .build());
                }

            });
            String customerName = customerService.getCustomerName(customer);
            if(customerName.contains(CJONS)) {
                if(customerName.contains("_")) {
                    customerName = customerName.split("_")[1];
                }else {
                    String prefix = customerName.substring(0, 5).replace(CJONS, "");
                    String suffix = customerName.substring(5);
                    customerName = (prefix + suffix).trim();
                    if(customerName.length() == 0) {
                        customerName = CJONS;
                    }
                }
            }
            result.add(CollectionStatusByCustomerDto.builder()
                    .customer(customerName)
                    .key(customerService.getCustomerName(customer).contains(CJONS) ? CJONS : CJ)
                    .data(dataList)
                    .build());
        });

        return result;
    }

    //고객사 목록
    public List<Customer> getCustomers() {
        String today = actualToday();
        String yesterday = actualYesterday();
        /*
            1. ES query를 통해 오늘날짜 고객사 데이터 불러오기
            2. CustomerService에 없는 고객사라면 인메모리 HashMap에 저장해서 불러오기 때문에 CustomerService에 등록되지 않더라도 고객사 표기가 됨.
         */
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(DATE, today))
                .addAggregation(AggregationBuilders.terms(CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms customerTerms = aggregations.get(CUSTOMER_FILTER);
        customerTerms.getBuckets().forEach(customer -> {
            if(customerService.getCustomer().get(customer.getKeyAsString()) == null) {
                customerService.createCustomerIfNotFound(customer.getKeyAsString());
            }
        });
        List<Customer> result = new ArrayList<>(customerService.getCustomer().values());
        Collections.sort(result, (o1, o2) -> o1.getName().compareTo(o2.getName()));

        getTypes();
        return result;
    }


    // 고객사별 현황 CSR.ver
    public StatusByCustomerDTO getStatusGroupByIPAndTypeAndStatusByCustomer(String customer) {
        if(customerService.getCustomerName(customer) == null) {
            throw new InvalidException(exceptionMessage("customer.not_found"));
        }
        String today = actualToday();
        String yesterday = actualYesterday();
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(CUSTOMER, customer))
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today))
                        .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(TODAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE)))))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(YESTERDAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE)))))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        List<String> defaultTypes = getTypes();
        List<String> defaultStatuses = getStatuses();
        Map<String, Map<String, Long>> todayStatusCountMap = new HashMap<>();
        List<StatusByCustomerDTO.TypeData> typeDataList = new ArrayList<>();
        Aggregations aggregations = searchHits.getAggregations();
        //2. 오늘 날짜 집계
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayIPTerms = todayFilter.getAggregations().get(TODAY_IP_FILTER);
        todayIPTerms.getBuckets().forEach(todayIPBucket -> {
            Terms typeTerms = todayIPBucket.getAggregations().get(TODAY_TYPE_FILTER);
            typeTerms.getBuckets().forEach(typeBucket -> {
                String type = dynamicExtractType(typeBucket.getKeyAsString());
                Terms statusTerms = typeBucket.getAggregations().get(TODAY_STATUS_FILTER);
                statusTerms.getBuckets().forEach(statusBucket -> {
                    String status = statusBucket.getKeyAsString();
                    long count = statusBucket.getDocCount();

                    todayStatusCountMap.computeIfAbsent(type, k -> new HashMap<>())
                            .merge(status, 1L, Long::sum);
                });
                for (String defaultStatus : defaultStatuses) {
                    todayStatusCountMap.get(type).putIfAbsent(defaultStatus, 0L);
                }
            });
        });
        //3. 어제 날짜 집계
        Map<String, Map<String, Long>> yesterdayStatusCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayIPTerms = yesterdayFilter.getAggregations().get(YESTERDAY_IP_FILTER);
        yesterdayIPTerms.getBuckets().forEach(yesterdayIPBucket -> {
            Terms typeTerms = yesterdayIPBucket.getAggregations().get(YESTERDAY_TYPE_FILTER);
            typeTerms.getBuckets().forEach(typeBucket -> {
                String type = dynamicExtractType(typeBucket.getKeyAsString());
                Terms statusTerms = typeBucket.getAggregations().get(YESTERDAY_STATUS_FILTER);
                statusTerms.getBuckets().forEach(statusBucket -> {
                    String status = statusBucket.getKeyAsString();
                    long count = statusBucket.getDocCount();

                    yesterdayStatusCountMap.computeIfAbsent(type, k -> new HashMap<>())
                            .merge(status, count, Long::sum);
                });
                for (String defaultStatus : defaultStatuses) {
                    yesterdayStatusCountMap.get(type).putIfAbsent(defaultStatus, 0L);
                }
            });
        });

        //4. 오늘 날짜에 해당 하는 Type이 없을 경우 0으로 초기화
        defaultTypes.forEach(defaultType -> {
            todayStatusCountMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                    .collect(Collectors.toMap(status -> status, count -> 0L)));
            yesterdayStatusCountMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                    .collect(Collectors.toMap(status -> status, count -> 0L)));
        });

        //5. 전일 대비 계산 및 DTO생성
        todayStatusCountMap.forEach((type, statusCountMap) -> {
            AtomicBoolean isSave = new AtomicBoolean(false);
            List<StatusByCustomerDTO.StatusData> statusDataList = new ArrayList<>();
            statusCountMap.forEach((status, todayCount) -> {
                if(type.equals(LINUX) || type.equals(WINDOWS) || type.equals(AIX) || type.equals(SAN_NET) || type.equals(STO)) {
                    isSave.set(true);
                    long yesterdayCount = 0;
                    if(yesterdayStatusCountMap.get(type) != null && yesterdayStatusCountMap.get(type).get(status) != null) {
                        yesterdayCount = yesterdayStatusCountMap.get(type).get(status);
                    }
                    statusDataList.add(StatusByCustomerDTO.StatusData.builder()
                            .status(status)
                            .todayCount(todayCount)
                            .yesterdayCount(yesterdayCount)
                            .build());
                }
            });
            if(isSave.get()) {
                typeDataList.add(StatusByCustomerDTO.TypeData.builder()
                        .type(type)
                        .data(statusDataList)
                        .build());
            }

        });

        return StatusByCustomerDTO.builder()
                .customer(customerService.getCustomerName(customer))
                .key(customerService.getCustomerName(customer).contains(CJONS) ? CJONS : CJ)
                .data(typeDataList)
                .build();

    }

    // 고객사별 현황 SSR.ver
    public List<StatusByCustomerDTO> getStatusGroupByCustomerAndIPAndTypeAndStatus() {
        //Customers Map 갱신
        String today = actualToday();
        String yesterday = actualYesterday();
        List<Customer> defaultCustomers = getCustomers();
        List<StatusByCustomerDTO> result = new ArrayList<>();
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today))
                        .subAggregation(AggregationBuilders.terms(TODAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                                .subAggregation(AggregationBuilders.terms(TODAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))))))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                                .subAggregation(AggregationBuilders.terms(YESTERDAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))))))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        List<String> defaultTypes = getTypes();
        List<String> defaultStatuses = getStatuses();
        Map<String, Map<String, Map<String, Long>>> todayCustomerCountMap = new HashMap<>();
        Aggregations aggregations = searchHits.getAggregations();
        //2. 오늘 날짜 집계
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayCustomerTerms = todayFilter.getAggregations().get(TODAY_CUSTOMER_FILTER);
        todayCustomerTerms.getBuckets().forEach(todayCustomerBucket -> {
            String customer = customerService.getCustomerName(todayCustomerBucket.getKeyAsString());
            Terms todayIPTerms = todayCustomerBucket.getAggregations().get(TODAY_IP_FILTER);
            todayIPTerms.getBuckets().forEach(todayIPBucket -> {
                Terms typeTerms = todayIPBucket.getAggregations().get(TODAY_TYPE_FILTER);
                typeTerms.getBuckets().forEach(typeBucket -> {
                    String type = dynamicExtractType(typeBucket.getKeyAsString());
                    Terms statusTerms = typeBucket.getAggregations().get(TODAY_STATUS_FILTER);
                    statusTerms.getBuckets().forEach(statusBucket -> {
                        String status = statusBucket.getKeyAsString();
                        long count = statusBucket.getDocCount();
//                        todayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
//                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, 1L, Long::sum);
                        todayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, count, Long::sum);

                    });
                });
            });
        });


        //3. 어제 날짜 집계
        Map<String, Map<String, Map<String, Long>>> yesterdayCustomerCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayCustomerTerms = yesterdayFilter.getAggregations().get(YESTERDAY_CUSTOMER_FILTER);
        yesterdayCustomerTerms.getBuckets().forEach(yesterdayCustomerBucket -> {
            String customer = customerService.getCustomerName(yesterdayCustomerBucket.getKeyAsString());
            Terms yesterdayIPTerms = yesterdayCustomerBucket.getAggregations().get(YESTERDAY_IP_FILTER);
            yesterdayIPTerms.getBuckets().forEach(yesterdayIPBucket -> {
                Terms typeTerms = yesterdayIPBucket.getAggregations().get(YESTERDAY_TYPE_FILTER);
                typeTerms.getBuckets().forEach(typeBucket -> {
                    String type = dynamicExtractType(typeBucket.getKeyAsString());
                    Terms statusTerms = typeBucket.getAggregations().get(YESTERDAY_STATUS_FILTER);
                    statusTerms.getBuckets().forEach(statusBucket -> {
                        String status = statusBucket.getKeyAsString();
                        long count = statusBucket.getDocCount();
//                        yesterdayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
//                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, 1L, Long::sum);
                        yesterdayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, count, Long::sum);
                    });
                });
            });
        });

        //4. 수집되지 않은 고객사 목록 추가
//        defaultCustomers.forEach(defaultCustomer -> {
//            todayCustomerCountMap.computeIfAbsent(defaultCustomer.getName(), t -> new HashMap<>());
//            yesterdayCustomerCountMap.computeIfAbsent(defaultCustomer.getName(), t -> new HashMap<>());
//        });

        //5. 수집 되지 않은 Type 의 Status를 0으로 채움
        defaultTypes.forEach(defaultType -> {
            todayCustomerCountMap.forEach((todayCustomer, typeMap) -> {
                typeMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                        .collect(Collectors.toMap(status -> status, count -> 0L)));
            });
            yesterdayCustomerCountMap.forEach((yesterdayCustomer, typeMap) -> {
                typeMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                        .collect(Collectors.toMap(status -> status, count -> 0L)));
            });
        });
        //6. 전일 대비 계산 및 DTO생성
        todayCustomerCountMap.forEach((todayCustomer, typeMap) -> {
            AtomicBoolean isSave = new AtomicBoolean(false);
            List<StatusByCustomerDTO.TypeData> typeDataList = new ArrayList<>();
            typeMap.forEach((type, statusCountMap) -> {
                List<StatusByCustomerDTO.StatusData> statusDataList = new ArrayList<>();
                statusCountMap.forEach((status, count) -> {
                    if(type.equals(LINUX) || type.equals(WINDOWS) || type.equals(AIX) || type.equals(SAN_NET) || type.equals(STO)) {
                        isSave.set(true);
                        long yesterdayCount = 0;

                        if(yesterdayCustomerCountMap.get(todayCustomer) != null &&
                                yesterdayCustomerCountMap.get(todayCustomer).get(type) != null &&
                                yesterdayCustomerCountMap.get(todayCustomer).get(type).get(status) != null) {
                            yesterdayCount = yesterdayCustomerCountMap.get(todayCustomer).get(type).get(status);
                        }
                        statusDataList.add(StatusByCustomerDTO.StatusData.builder()
                                .status(status)
                                .todayCount(count)
                                .yesterdayCount(yesterdayCount)
                                .build());
                    }
                });
                if(isSave.get() && type.equals(LINUX) || type.equals(WINDOWS) || type.equals(AIX) || type.equals(SAN_NET) || type.equals(STO)) {
                    typeDataList.add(StatusByCustomerDTO.TypeData.builder()
                            .type(type)
                            .data(statusDataList)
                            .build());

                }
            });
            if(!typeDataList.isEmpty()) {
                result.add(StatusByCustomerDTO.builder()
                        .customer(todayCustomer)
                        .key(todayCustomer.contains(CJONS) ? CJONS : CJ)
                        .data(typeDataList)
                        .build());
            }


        });

        return result;

    }

    // 고객사별 Abnormal 건수 추이
    public List<AbnormalStatusByCustomerDto> getAbnormalStatusGroupByCustomer() {
        String today = actualToday();
        String sevenDaysAgo = actualSevenDaysAgo();
        List<AbnormalStatusByCustomerDto> results = new ArrayList<>();
        //1. ES query 8일전 ~ 오늘 까지 집계 (8일 전까지 불러오는 이유는 7일전 데이터에서 전일대비 계산을 위해)
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery(DATE)
                                .gte(sevenDaysAgo)
                                .lte(today))
                        .must(QueryBuilders.matchQuery(STATUS, ABNORMAL)))
                .addAggregation(AggregationBuilders.terms(DATE_FILTER).field(DATE).size(Integer.MAX_VALUE)
                        .subAggregation(AggregationBuilders.terms(CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)))
                .build();

        List<Customer> defaultCustomers = getCustomers();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms dateTerms = aggregations.get(DATE_FILTER);
        //2. 오늘부터 8일전 데이터 까지 집계
        Map<String, Map<String, Long>> dateCustomerCountMap = new HashMap<>();
        dateTerms.getBuckets().forEach(dateBucket -> {
            //String date = dateBucket.getKeyAsString();
            String date = LocalDateTime.parse(dateBucket.getKeyAsString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern(DAY_FORMAT));
            Terms customerTerms = dateBucket.getAggregations().get(CUSTOMER_FILTER);
            customerTerms.getBuckets().forEach(customerBucket -> {
                String customer = customerService.getCustomerName(customerBucket.getKeyAsString());
                long count = customerBucket.getDocCount();
                dateCustomerCountMap.computeIfAbsent(date, k -> new HashMap<>())
                        .merge(customer, count, Long::sum);

            });
            for (Customer defaultCustomer : defaultCustomers) {
                dateCustomerCountMap.get(date).putIfAbsent(defaultCustomer.getName(), 0L);
            }

        });
        List<CustomerDto> todayCustomerList = new ArrayList<>();
        dateCustomerCountMap.computeIfAbsent(today, k -> new HashMap<String, Long>());
        dateCustomerCountMap.get(today).forEach((customer, count) -> {
            todayCustomerList.add(new CustomerDto(customer, count));
        });


        Collections.sort(todayCustomerList, (o1, o2) -> Math.toIntExact(o2.getCount() - o1.getCount()));
        //3. 날짜 별로 전일대비 계산 및 DTO 생성
        getDatesBetween()
                .forEach(date -> {
                    List<AbnormalStatusByCustomerDto.Abnormal> abnormalList = new ArrayList<>();
                    dateCustomerCountMap.putIfAbsent(date, todayCustomerList.stream()
                            .collect(Collectors.toMap(customer -> customer.getName(), count -> 0L)));

                    Map<String, Long> yesterdayData = dateCustomerCountMap.getOrDefault(getYesterdayToString(date), Collections.emptyMap());
                    Map<String, Long> todayData = dateCustomerCountMap.getOrDefault(date, Collections.emptyMap());
                    AtomicInteger top = new AtomicInteger();
                    todayCustomerList.forEach(customer -> {
                        if(top.get() >= topCount) return;
                        String customerName = customer.getName();
                        Long count = todayData.getOrDefault(customerName, 0L);
                        Long yesterdayCount = yesterdayData.getOrDefault(customerName, 0L);
                        abnormalList.add(AbnormalStatusByCustomerDto.Abnormal.builder()
                                .customer(customerName)
                                .count(count)
                                .yesterdayCount(yesterdayCount)
                                .build());
                        top.getAndIncrement();
                    });


                    results.add(AbnormalStatusByCustomerDto.builder()
                            .date(date)
                            .data(abnormalList)
                            .build());
                });

        Collections.sort(results, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));
        return results;
    }

    // Uptime 분포도
    public List<UptimeChartDTO> getUptimeChart() {
        String today = actualToday();
        //1. ES query
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery(DATE, today))
                        .must(QueryBuilders.matchQuery(GROUP, UPTIME)))
                .build();

        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        return searchHits.stream()
                .filter(uptime -> uptime.getContent().getCustomer() != null)
                .map(uptime ->
                        UptimeChartDTO.builder()
                                .customer(customerService.getCustomerName(uptime.getContent().getCustomer()))
                                .ip(uptime.getContent().getIp())
                                .limit(uptime.getContent().getOriginal())
                                .uptime(uptime.getContent().getCurrent())
                                .key(customerService.getCustomerName(uptime.getContent().getCustomer()).contains(CJONS) ? CJONS : CJ)
                                .status(uptime.getContent().getStatus()).build())
                .collect(Collectors.toList());
    }

    // 그룹 별 Abnormal 전체 건수별
    public List<AbnormalStatusByTypeDto> getAbnormalStatusGroupByType() {
        List<AbnormalStatusByTypeDto> result = new ArrayList<>();
        String today = actualToday();
        String yesterday = actualYesterday();

        // 1. ES query
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(STATUS, ABNORMAL))
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today))
                        .subAggregation(AggregationBuilders.terms(TODAY_GROUP_FILTER).field(GROUP_KEYWORD).size(Integer.MAX_VALUE)
                                        .order(BucketOrder.count(false)) // count 기준 내림차순 정렬
                                        .size(topCount))) // 상위 10개만 가져오기

                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_GROUP_FILTER).field(GROUP_KEYWORD).size(Integer.MAX_VALUE)
                                        .order(BucketOrder.count(false)) // count 기준 내림차순 정렬
                                        .size(topCount))) // 상위 10개만 가져오기
                .build();

        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();

        // 2. 오늘 날짜 집계
        Map<String, Long> todayTypeCountMap = new HashMap<>();
        Filter todayFilter = aggregations.get(TODAY_AGG);

        Terms todayGroupTerms = todayFilter.getAggregations().get(TODAY_GROUP_FILTER);
        todayGroupTerms.getBuckets().forEach(todayGroupBucket -> {
            String group = todayGroupBucket.getKeyAsString();
            todayTypeCountMap.put(group, todayTypeCountMap.getOrDefault(group, 0L) + todayGroupBucket.getDocCount());
        });


        // 3. 어제 날짜 집계
        Map<String, Long> yesterdayTypeCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayGroupTerms = yesterdayFilter.getAggregations().get(YESTERDAY_GROUP_FILTER);
        yesterdayGroupTerms.getBuckets().forEach(yesterdayGroupBucket -> {
            String group = yesterdayGroupBucket.getKeyAsString();
            yesterdayTypeCountMap.put(group, yesterdayTypeCountMap.getOrDefault(group, 0L) + yesterdayGroupBucket.getDocCount());
        });

        // 4. 전일 대비 계산 및 DTO 생성
        todayTypeCountMap.forEach((group, todayCount) -> {
            long yesterdayCount = 0;
            if (yesterdayTypeCountMap.get(group) != null) {
                yesterdayCount = yesterdayTypeCountMap.get(group);
            }
            result.add(AbnormalStatusByTypeDto.builder()
                    .group(group)
                    .todayCount(todayCount)
                    .yesterdayCount(yesterdayCount)
                    .build());
        });

        Collections.sort(result, (o1, o2) -> Math.toIntExact(o2.getTodayCount() - o1.getTodayCount()));
        return result.size() > topCount ? result.subList(0, topCount) : result;
    }

    private String dynamicExtractType(String type) {
        if(type != null && type.contains("_")) {
            if(type.contains(OSS)) {
                return type.split("_")[1];
            }else if(type.split("_")[0].contains(SAN) || type.split("_")[0].contains(NET)) {
                return SAN_NET;
            }else {
                return type.split("_")[0];
            }
        }
        return type;
    }

    private List<String> getTypes() {
        String today = actualToday();
        String yesterday = actualYesterday();
        List<String> result = new ArrayList<>();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today))
                .addAggregation(AggregationBuilders.terms(TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms typeTerms = aggregations.get(TYPE_FILTER);
        typeTerms.getBuckets().forEach(type -> {
            if(typeService.getType().get(type.getKeyAsString()) == null) {
                typeService.createTypeIfNotFound(type.getKeyAsString());
            }
        });
        typeService.getType().forEach((typeId, type) -> result.add(typeId));
        return result;
    }

    private List<String> getStatuses() {
        String today = actualToday();
        String yesterday = actualYesterday();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today))
                .addAggregation(AggregationBuilders.terms(STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms customerTerms = aggregations.get(STATUS_FILTER);
        return customerTerms.getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                .collect(Collectors.toList());
    }

    private String actualToday() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        //오늘 데이터가 없으면 00시 ~ 09시 전날 데이터를 보여줌
        if(searchHits.getTotalHits() == 0) {
            if(mode.equals("test")) {
                return staticDate.minusDays(1).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
            }
            return LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
        }

        return today();
    }

    private String actualYesterday() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        //오늘 데이터가 없으면 00시 ~ 09시 전날 데이터를 보여줌
        if(searchHits.getTotalHits() == 0) {
            if(mode.equals("test")) {
                return staticDate.minusDays(2).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
            }
            return LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
        }

        return yesterday();
    }

    private String actualSevenDaysAgo() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        if(searchHits.getTotalHits() == 0) {
            if(mode.equals("test")) {
                return staticDate.minusDays(8).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
            }
            return LocalDateTime.now().minusDays(8).format(DateTimeFormatter.ofPattern(DAY_FORMAT));
        }
        return sevenDaysAgo();
    }
    private List<String> getDatesBetween() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAY_FORMAT);
        LocalDate startDate = null;
        LocalDate endDate = null;
        if(searchHits.getTotalHits() == 0) {
            if(mode.equals("test")) {
                startDate = staticDate.minusDays(7);
                endDate = staticDate.minusDays(1);
            }else {
                startDate = LocalDate.now().minusDays(7);
                endDate = LocalDate.now().minusDays(1);
            }
        }else {
            if(mode.equals("test")) {
                startDate = staticDate.minusDays(6);
                endDate = staticDate;
            }else {
                startDate = LocalDate.now().minusDays(6);
                endDate = LocalDate.now();
            }
        }

        while (!startDate.isAfter(endDate)) {
            dates.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }

        return dates;
    }
}
