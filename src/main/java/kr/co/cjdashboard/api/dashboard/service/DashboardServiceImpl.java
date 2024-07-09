package kr.co.cjdashboard.api.dashboard.service;

import kr.co.cjdashboard.api.dashboard.entity.CjLog;
import kr.co.cjdashboard.api.dashboard.model.*;
import kr.co.cjdashboard.exception.InvalidException;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.composite.ParsedComposite;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static kr.co.cjdashboard.api.dashboard.constant.FieldName.*;
import static kr.co.cjdashboard.api.dashboard.constant.FieldName.YESTERDAY_IP_FILTER;
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
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final CustomerService customerService;
    private final TypeService typeService;

    //전체 등록 현황
    public TotalRegistrationStatusDto getTotalRegistrationStatusGroupByIPAndType() {
        Map<String, Long> result = new HashMap<>();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .addAggregation(AggregationBuilders.terms(IP_FILTER).field(IP_KEYWORD)
                        .subAggregation(AggregationBuilders.terms(TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE))
                .withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{}))
                .build();

        List<String> defaultTypes = new ArrayList<>();
        getTypes().forEach(defaultType -> defaultTypes.add(dynamicExtractType(defaultType)));
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms ipTerms = aggregations.get(IP_FILTER);
        ipTerms.getBuckets().forEach(ipBucket -> {
            Terms typeTerms = ipBucket.getAggregations().get(TYPE_FILTER);
            typeTerms.getBuckets().forEach(typeBucket -> {
                String type = dynamicExtractType(typeBucket.getKeyAsString());
                result.put(type, result.getOrDefault(type, 0L) + 1);
            });
        });

        defaultTypes.forEach(defaultType -> {
            String type = dynamicExtractType(defaultType);

            result.put(type, result.getOrDefault(type, 0L));
        });

        TotalRegistrationStatusDto totalRegistrationStatusDto = new TotalRegistrationStatusDto();
        Map<String, Long> ossList = new HashMap<>();
        result.forEach((type, count) -> {
            if(type.equals(STO)) {
                totalRegistrationStatusDto.setSTO(count);
            } else if(type.equals(SAN_NET)) {
                totalRegistrationStatusDto.setSAN_NET(count);
            } else {
                //OSS
                ossList.put(type, count);
            }
        });
        totalRegistrationStatusDto.setOSS(ossList);
        return totalRegistrationStatusDto;
    }

    // 고객사별 수집 현황
    public List<CollectionStatusByCustomerDto> getCollectionStatusGroupByCustomerAndType() {
        Query query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today()))
                        .subAggregation(AggregationBuilders.terms(TODAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE)))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday()))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD)).size(Integer.MAX_VALUE)))
                .build();

        List<Customer> defaultCustomers = getCustomers();
        List<String> defaultTypes = new ArrayList<>();
        getTypes().forEach(defaultType -> defaultTypes.add(dynamicExtractType(defaultType)));
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();

        Map<String, Map<String, Long>> todayStatusCountMap = new HashMap<>();
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayCustomerTerms = todayFilter.getAggregations().get(TODAY_CUSTOMER_FILTER);
        todayCustomerTerms.getBuckets()
                .forEach(todayCustomerBucket -> {
                    String customer = todayCustomerBucket.getKeyAsString();
                    Terms typeTerms = todayCustomerBucket.getAggregations().get(TODAY_TYPE_FILTER);
                    typeTerms.getBuckets().forEach(typeBucket -> {

                        String type = dynamicExtractType(typeBucket.getKeyAsString());
                        long count = typeBucket.getDocCount();
                        todayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
                                .merge(type, count, Long::sum);
                    });
                    defaultTypes.forEach(defaultType ->
                            todayStatusCountMap.get(customer).putIfAbsent(defaultType, 0L));
                });
        Map<String, Map<String, Long>> yesterdayStatusCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayCustomerTerms = yesterdayFilter.getAggregations().get(YESTERDAY_CUSTOMER_FILTER);
        yesterdayCustomerTerms.getBuckets()
                .forEach(yesterdayCustomerBucket -> {
                    String customer = yesterdayCustomerBucket.getKeyAsString();
                    Terms typeTerms = yesterdayCustomerBucket.getAggregations().get(YESTERDAY_TYPE_FILTER);
                    typeTerms.getBuckets().forEach(typeBucket -> {
                        String type = dynamicExtractType(typeBucket.getKeyAsString());
                        long count = typeBucket.getDocCount();
                        yesterdayStatusCountMap.computeIfAbsent(customer, k -> new HashMap<>())
                                .merge(type, count, Long::sum);
                    });
                    defaultTypes.forEach(defaultType ->
                            yesterdayStatusCountMap.get(customer).putIfAbsent(defaultType, 0L));
                });
        //기본 Type 삽입
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
            result.add(CollectionStatusByCustomerDto.builder()
                    .customer(customerService.getCustomerName(customer))
                    .data(dataList)
                    .build());
        });

        return result;
    }

    //고객사 목록
    public List<Customer> getCustomers() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(DATE, today()))
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

        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(CUSTOMER, customer))
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today()))
                        .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(TODAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE)))))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday()))
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
        //오늘 자 집계
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
        //어제 자 집계
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

        //기본 Type 삽입
        defaultTypes.forEach(defaultType -> {
            todayStatusCountMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                    .collect(Collectors.toMap(status -> status, count -> 0L)));
            yesterdayStatusCountMap.putIfAbsent(dynamicExtractType(defaultType), defaultStatuses.stream()
                    .collect(Collectors.toMap(status -> status, count -> 0L)));
        });

        //전일 대비 계산 및 DTO생성
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
                .data(typeDataList)
                .build();

    }

    // 고객사별 현황 SSR.ver
    public List<StatusByCustomerDTO> getStatusGroupByCustomerAndIPAndTypeAndStatus() {
        //Customers Map 갱신
        List<Customer> defaultCustomers = getCustomers();
        List<StatusByCustomerDTO> result = new ArrayList<>();
        Query query = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today()))
                        .subAggregation(AggregationBuilders.terms(TODAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(TODAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                                .subAggregation(AggregationBuilders.terms(TODAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))))))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday()))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)
                                .subAggregation(AggregationBuilders.terms(YESTERDAY_IP_FILTER).field(IP_KEYWORD).size(Integer.MAX_VALUE)
                                        .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)
                                                .subAggregation(AggregationBuilders.terms(YESTERDAY_STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))))))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);

        List<String> defaultTypes = getTypes();
        List<String> defaultStatuses = getStatuses();
        //Map<String, Map<String, Long>> todayStatusCountMap = new HashMap<>();
        Map<String, Map<String, Map<String, Long>>> todayCustomerCountMap = new HashMap<>();
        Aggregations aggregations = searchHits.getAggregations();
        //오늘 자 집계
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
                        todayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, 1L, Long::sum);

                    });
                });
            });
        });


        //어제 자 집계
        //Map<String, Map<String, Long>> yesterdayStatusCountMap = new HashMap<>();
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
                        yesterdayCustomerCountMap.computeIfAbsent(customer, t -> new HashMap<>())
                                .computeIfAbsent(type, c -> new HashMap<>()).merge(status, 1L, Long::sum);
                    });
                });
            });
        });


        //수집되지 않은 고객사 목록 추가
        defaultCustomers.forEach(defaultCustomer -> {
            todayCustomerCountMap.computeIfAbsent(defaultCustomer.getName(), t -> new HashMap<>());
            yesterdayCustomerCountMap.computeIfAbsent(defaultCustomer.getName(), t -> new HashMap<>());
        });
        //수집 되지 않은 Type 의 Status를 0으로 채움
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
        //전일 대비 계산 및 DTO생성
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
                        .data(typeDataList)
                        .build());
            }


        });

        return result;

    }

    // 고객사별 Abnormal 건수 추이
    public List<AbnormalStatusByCustomerDto> getAbnormalStatusGroupByCustomer() {
        List<AbnormalStatusByCustomerDto> results = new ArrayList<>();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery(DATE)
                                .gte(sevenDaysAgo())
                                .lte(today()))
                        .must(QueryBuilders.matchQuery(STATUS, ABNORMAL)))
                .addAggregation(AggregationBuilders.terms(DATE_FILTER).field(DATE_KEYWORD).size(Integer.MAX_VALUE)
                        .subAggregation(AggregationBuilders.terms(CUSTOMER_FILTER).field(CUSTOMER_KEYWORD).size(Integer.MAX_VALUE)).size(Integer.MAX_VALUE))
                .build();

        List<Customer> defaultCustomers = getCustomers();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms dateTerms = aggregations.get(DATE_FILTER);

        Map<String, Map<String, Long>> dateCustomerCountMap = new HashMap<>();
        dateTerms.getBuckets().forEach(dateBucket -> {
            String date = dateBucket.getKeyAsString();

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

        getDatesBetween()
                .forEach(date -> {
                    List<AbnormalStatusByCustomerDto.Abnormal> abnormalList = new ArrayList<>();
                    dateCustomerCountMap.putIfAbsent(date, defaultCustomers.stream()
                            .collect(Collectors.toMap(customer -> customer.getName(), count -> 0L)));

                    Map<String, Long> yesterdayData = dateCustomerCountMap.getOrDefault(getYesterdayToString(date), Collections.emptyMap());
                    dateCustomerCountMap.getOrDefault(date, Collections.emptyMap())
                            .forEach((customer, count) -> {
                                abnormalList.add(AbnormalStatusByCustomerDto.Abnormal.builder()
                                        .customer(customer)
                                        .count(count)
                                        .yesterdayCount(yesterdayData.getOrDefault(customer, 0L))
                                        .build());
                            });

                    results.add(AbnormalStatusByCustomerDto.builder()
                            .date(date)
                            .data(abnormalList)
                            .build());
                });

        return results;
    }

    // Uptime 분포도
    public List<UptimeChartDTO> getUptimeChart() {
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery(DATE, today()))
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
                                .uptime(uptime.getContent().getCurrent()).build())
                .collect(Collectors.toList());
    }

    // 항목 별 Abnormal 전체 건수별
    public List<AbnormalStatusByTypeDto> getAbnormalStatusGroupByType() {
        List<AbnormalStatusByTypeDto> result = new ArrayList<>();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery(STATUS, ABNORMAL))
                .addAggregation(AggregationBuilders.filter(TODAY_AGG,
                                QueryBuilders.matchQuery(DATE, today()))
                        .subAggregation(AggregationBuilders.terms(TODAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)))
                .addAggregation(AggregationBuilders.filter(YESTERDAY_AGG,
                                QueryBuilders.matchQuery(DATE, yesterday()))
                        .subAggregation(AggregationBuilders.terms(YESTERDAY_TYPE_FILTER).field(TYPE_KEYWORD).size(Integer.MAX_VALUE)))
                .build();

        List<String> defaultTypes = getTypes();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();

        //오늘 자 집계
        Map<String, Long> todayTypeCountMap = new HashMap<>();
        Filter todayFilter = aggregations.get(TODAY_AGG);
        Terms todayTypeTerms = todayFilter.getAggregations().get(TODAY_TYPE_FILTER);
        todayTypeTerms.getBuckets().forEach(typeBucket -> {
            String type = dynamicExtractType(typeBucket.getKeyAsString());
            todayTypeCountMap.put(type, todayTypeCountMap.getOrDefault(type, 0L) + typeBucket.getDocCount());
        });

        //어제 자 집계
        Map<String, Long> yesterdayTypeCountMap = new HashMap<>();
        Filter yesterdayFilter = aggregations.get(YESTERDAY_AGG);
        Terms yesterdayTypeTerms = yesterdayFilter.getAggregations().get(YESTERDAY_TYPE_FILTER);
        yesterdayTypeTerms.getBuckets().forEach(typeBucket -> {
            String type = dynamicExtractType(typeBucket.getKeyAsString());
            yesterdayTypeCountMap.put(type, yesterdayTypeCountMap.getOrDefault(type, 0L) + typeBucket.getDocCount());
        });
        defaultTypes.forEach(defaultType -> {
            String type = dynamicExtractType(defaultType);
            todayTypeCountMap.put(type, todayTypeCountMap.getOrDefault(type, 0L));
            yesterdayTypeCountMap.put(type, yesterdayTypeCountMap.getOrDefault(type, 0L));
        });

        todayTypeCountMap.forEach((type, todayCount) -> {
            if(type.equals(LINUX) || type.equals(WINDOWS) || type.equals(AIX) || type.equals(SAN_NET) || type.equals(STO)) {
                long yesterdayCount = 0;
                if(yesterdayTypeCountMap.get(type) != null) {
                    yesterdayCount = yesterdayTypeCountMap.get(type);
                }
                result.add(AbnormalStatusByTypeDto.builder()
                        .type(type)
                        .todayCount(todayCount)
                        .yesterdayCount(yesterdayCount)
                        .build());
            }
        });

        return result;
    }

    private String extractTypePrefix(String type) {
        if (type != null && type.contains("_")) {
            return type.split("_")[0];
        }else if(type.contains(SAN) || type.contains(NET)) {
            return SAN_NET;
        }
        return type;
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
        List<String> result = new ArrayList<>();
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
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
        Query query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery(DATE, today()))
                .addAggregation(AggregationBuilders.terms(STATUS_FILTER).field(STATUS_KEYWORD).size(Integer.MAX_VALUE))
                .build();
        SearchHits<CjLog> searchHits = elasticsearchRestTemplate.search(query, CjLog.class);
        Aggregations aggregations = searchHits.getAggregations();
        Terms customerTerms = aggregations.get(STATUS_FILTER);
        return customerTerms.getBuckets().stream()
                .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                .collect(Collectors.toList());
    }

}
