package ru.practicum.ewm.main.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.NewEventDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.Location;

@Configuration
public class WebConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<NewEventDto, Event> propertyNewEventDtoToEvent = modelMapper.createTypeMap(NewEventDto.class, Event.class);
        propertyNewEventDtoToEvent.addMapping(o -> o.getLocation().getLat(), Event::setLat);
        propertyNewEventDtoToEvent.addMapping(o -> o.getLocation().getLon(), Event::setLon);

        TypeMap<Event, EventFullDto> propertyEventToEventFullDto = modelMapper.createTypeMap(Event.class, EventFullDto.class);
        Converter<Event, Location> converterLocation = c -> new Location(c.getSource().getLat(), c.getSource().getLon());
        propertyEventToEventFullDto.addMappings(
                new PropertyMap<>() {
                    @Override
                    protected void configure() {
                        using(converterLocation).map(source, destination.getLocation());
                    }
                }
        );

        return modelMapper;
    }
}