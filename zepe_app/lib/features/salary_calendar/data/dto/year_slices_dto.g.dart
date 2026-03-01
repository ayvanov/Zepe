// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'year_slices_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

YearSlicesDto _$YearSlicesDtoFromJson(Map<String, dynamic> json) =>
    YearSlicesDto(
      year: (json['year'] as num).toInt(),
      months:
          (json['months'] as List<dynamic>).map((e) => e as String).toList(),
    );

Map<String, dynamic> _$YearSlicesDtoToJson(YearSlicesDto instance) =>
    <String, dynamic>{
      'year': instance.year,
      'months': instance.months,
    };
