import '../../domain/entities/year_slices.dart';
import '../dto/year_slices_dto.dart';

/// Mapper between DTO and domain entities
class YearSlicesMapper {
  /// Map DTO to domain entity
  static YearSlices toEntity(YearSlicesDto dto) {
    return YearSlices(
      year: dto.year,
      months: dto.months,
    );
  }

  /// Map domain entity to DTO
  static YearSlicesDto toDto(YearSlices entity) {
    return YearSlicesDto(
      year: entity.year,
      months: entity.months,
    );
  }

  /// Parse API response to entity
  static YearSlices fromApiData(int year, String rawData) {
    final dto = YearSlicesDto.fromApiData(year, rawData);
    return toEntity(dto);
  }
}
